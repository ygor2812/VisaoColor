package com.visaocolor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.visaocolor.R
import com.visaocolor.controllers.ColorIdentificationController
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.models.ObjectDetection
import com.visaocolor.repositories.LocalStorageRepository
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.services.BrettelFilterProcessor
import com.visaocolor.services.ColorAnalyzer
import com.visaocolor.services.ObjectDetectionEngine
import com.visaocolor.services.SpeechSynthesizer
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var executorCamera: ExecutorService
    private val processadorFiltro = BrettelFilterProcessor()
    private lateinit var armazenamento: LocalStorageRepository

    // identificacao de cor por toque (Modulo 3)
    private val controladorCor = ColorIdentificationController(
        ColorAnalyzer(),
        SessionColorRepository()
    )

    // IA e voz (Modulo 4)
    private lateinit var motorIA: ObjectDetectionEngine
    private lateinit var locutor: SpeechSynthesizer

    private lateinit var imagemCamera: ImageView
    private lateinit var textoStatus: TextView
    private lateinit var textoCor: TextView
    private lateinit var marcador: View
    private lateinit var painelAjustes: View

    @Volatile private var ultimoOriginal: Bitmap? = null

    private var tipoAtual: ColorBlindnessType = ColorBlindnessType.DEUTERANOPIA
    private var filtroLigado = true

    private var brilho = 0
    private var contraste = 100
    private var intensidade = 100

    // estado da IA e voz
    private var iaLigada = false
    private var vozLigada = false
    @Volatile private var deteccoes: List<ObjectDetection> = emptyList()
    private var ultimaDeteccaoMs = 0L
    private var ultimosNomes: Set<String> = emptySet()

    private val permissaoCamera = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedida ->
        if (concedida) iniciarCamera()
        else Toast.makeText(this, "Sem permissao de camera o app nao funciona", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imagemCamera = findViewById(R.id.imagemCamera)
        textoStatus = findViewById(R.id.textoStatus)
        textoCor = findViewById(R.id.textoCor)
        marcador = findViewById(R.id.marcador)
        painelAjustes = findViewById(R.id.painelAjustes)

        armazenamento = LocalStorageRepository(applicationContext)
        executorCamera = Executors.newSingleThreadExecutor()

        // prepara IA e voz
        motorIA = ObjectDetectionEngine(applicationContext)
        locutor = SpeechSynthesizer(applicationContext)
        locutor.iniciar()
        // carrega o modelo em segundo plano para nao travar a tela
        Thread {
            try {
                motorIA.carregar()
                Log.d("VisaoColor", "Modelo de IA carregado")
            } catch (e: Exception) {
                Log.e("VisaoColor", "Falha ao carregar modelo de IA", e)
            }
        }.start()

        prepararMarcador()
        configurarToque()
        carregarConfiguracoes()
        configurarControles()
        configurarSliders()
        solicitarPermissaoCamera()
    }

    private fun prepararMarcador() {
        val anel = GradientDrawable()
        anel.shape = GradientDrawable.OVAL
        anel.setColor(Color.TRANSPARENT)
        anel.setStroke(8, Color.WHITE)
        marcador.background = anel
    }

    private fun configurarToque() {
        imagemCamera.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                identificarPelaTela(event.x, event.y)
                v.performClick()
            }
            true
        }
    }

    private fun identificarPelaTela(tocX: Float, tocY: Float) {
        val bmp = ultimoOriginal ?: return
        val vw = imagemCamera.width.toFloat()
        val vh = imagemCamera.height.toFloat()
        if (vw <= 0f || vh <= 0f) return

        val escala = maxOf(vw / bmp.width, vh / bmp.height)
        val offX = (vw - bmp.width * escala) / 2f
        val offY = (vh - bmp.height * escala) / 2f

        val bx = ((tocX - offX) / escala).toInt().coerceIn(0, bmp.width - 1)
        val by = ((tocY - offY) / escala).toInt().coerceIn(0, bmp.height - 1)

        val pixel = bmp.getPixel(bx, by)
        val registro = controladorCor.identificar(Color.red(pixel), Color.green(pixel), Color.blue(pixel))

        textoCor.text = "${registro.nome}   ${registro.paraHex()}"
        textoCor.visibility = View.VISIBLE

        marcador.translationX = tocX - marcador.width / 2f
        marcador.translationY = tocY - marcador.height / 2f
        marcador.visibility = View.VISIBLE
    }

    private fun carregarConfiguracoes() = lifecycleScope.launch {
        val config = armazenamento.obterConfiguracoesImagem()
        brilho = config.brilho
        contraste = config.contraste
        intensidade = config.intensidade

        findViewById<SeekBar>(R.id.sliderBrilho).progress = brilho + 100
        findViewById<SeekBar>(R.id.sliderContraste).progress = contraste
        findViewById<SeekBar>(R.id.sliderIntensidade).progress = intensidade
        atualizarLabels()
    }

    private fun configurarControles() {
        findViewById<Button>(R.id.botaoProtanopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.PROTANOPIA
            atualizarStatus()
        }
        findViewById<Button>(R.id.botaoDeuteranopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.DEUTERANOPIA
            atualizarStatus()
        }
        findViewById<Button>(R.id.botaoTritanopia).setOnClickListener {
            tipoAtual = ColorBlindnessType.TRITANOPIA
            atualizarStatus()
        }

        val botaoLigaDesliga = findViewById<Button>(R.id.botaoLigaDesliga)
        botaoLigaDesliga.setOnClickListener {
            filtroLigado = !filtroLigado
            botaoLigaDesliga.text = if (filtroLigado) "Filtro: LIGADO" else "Filtro: DESLIGADO"
            atualizarStatus()
        }

        findViewById<Button>(R.id.botaoAjustes).setOnClickListener {
            painelAjustes.visibility =
                if (painelAjustes.visibility == View.GONE) View.VISIBLE else View.GONE
        }

        findViewById<Button>(R.id.botaoRestaurar).setOnClickListener {
            brilho = 0; contraste = 100; intensidade = 100
            findViewById<SeekBar>(R.id.sliderBrilho).progress = 100
            findViewById<SeekBar>(R.id.sliderContraste).progress = 100
            findViewById<SeekBar>(R.id.sliderIntensidade).progress = 100
            atualizarLabels()
            salvarConfiguracoes()
        }

        // botao liga/desliga a IA (reconhecimento de objetos)
        val botaoIA = findViewById<Button>(R.id.botaoIA)
        botaoIA.setOnClickListener {
            iaLigada = !iaLigada
            botaoIA.text = if (iaLigada) "IA: LIGADA" else "IA: OFF"
            if (!iaLigada) deteccoes = emptyList()
        }

        // botao liga/desliga a voz
        val botaoVoz = findViewById<Button>(R.id.botaoVoz)
        botaoVoz.setOnClickListener {
            vozLigada = !vozLigada
            locutor.definirAtivo(vozLigada)
            botaoVoz.text = if (vozLigada) "Voz: LIGADA" else "Voz: OFF"
        }

        atualizarStatus()
    }

    private fun configurarSliders() {
        findViewById<SeekBar>(R.id.sliderBrilho).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    brilho = valor - 100; atualizarLabels()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { salvarConfiguracoes() }
            })
        findViewById<SeekBar>(R.id.sliderContraste).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    contraste = valor; atualizarLabels()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { salvarConfiguracoes() }
            })
        findViewById<SeekBar>(R.id.sliderIntensidade).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    intensidade = valor; atualizarLabels()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { salvarConfiguracoes() }
            })
    }

    private fun atualizarLabels() {
        findViewById<TextView>(R.id.labelBrilho).text = "Brilho: $brilho"
        findViewById<TextView>(R.id.labelContraste).text = "Contraste: $contraste"
        findViewById<TextView>(R.id.labelIntensidade).text = "Intensidade: $intensidade"
    }

    private fun salvarConfiguracoes() = lifecycleScope.launch {
        armazenamento.salvarBrilho(brilho)
        armazenamento.salvarContraste(contraste)
        armazenamento.salvarIntensidade(intensidade)
    }

    private fun atualizarStatus() {
        val nomeFiltro = if (filtroLigado) tipoAtual.nomeExibicao else "Original"
        textoStatus.text = "Perfil: $nomeFiltro"
    }

    private fun solicitarPermissaoCamera() {
        val check = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (check == PackageManager.PERMISSION_GRANTED) iniciarCamera()
        else permissaoCamera.launch(Manifest.permission.CAMERA)
    }

    private fun iniciarCamera() {
        val futuroProvedor = ProcessCameraProvider.getInstance(this)
        futuroProvedor.addListener({
            val provedor = futuroProvedor.get()
            val analise = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
            analise.setAnalyzer(executorCamera) { imagem -> processarFrame(imagem) }
            try {
                provedor.unbindAll()
                provedor.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, analise)
            } catch (e: Exception) {
                Log.e("VisaoColor", "Erro ao iniciar a camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processarFrame(imagem: ImageProxy) {
        val rotacao = imagem.imageInfo.rotationDegrees
        val original = rotacionarBitmap(imagem.toBitmap(), rotacao)
        ultimoOriginal = original

        // roda a IA no maximo a cada 600ms (a inferencia e pesada)
        if (iaLigada) {
            val agora = System.currentTimeMillis()
            if (agora - ultimaDeteccaoMs > 600) {
                ultimaDeteccaoMs = agora
                try {
                    deteccoes = motorIA.detectar(original)
                    anunciar(deteccoes)
                } catch (e: Exception) {
                    Log.e("VisaoColor", "Erro na deteccao", e)
                }
            }
        }

        val tipoUsar = if (filtroLigado) tipoAtual else ColorBlindnessType.NONE
        var bitmapFinal = processadorFiltro.aplicar(original, tipoUsar, brilho, contraste, intensidade)

        // desenha as caixas dos objetos por cima da imagem
        if (iaLigada && deteccoes.isNotEmpty()) {
            bitmapFinal = desenharCaixas(bitmapFinal, deteccoes)
        }

        runOnUiThread { imagemCamera.setImageBitmap(bitmapFinal) }
        imagem.close()
    }

    // desenha um retangulo fino e o nome de cada objeto detectado
    private fun desenharCaixas(base: Bitmap, lista: List<ObjectDetection>): Bitmap {
        val bmp = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp)

        // linha fina do retangulo
        val caneta = Paint()
        caneta.color = Color.GREEN
        caneta.style = Paint.Style.STROKE
        caneta.strokeWidth = 3f
        caneta.isAntiAlias = true

        // texto do nome
        val texto = Paint()
        texto.color = Color.GREEN
        texto.textSize = 34f
        texto.style = Paint.Style.FILL
        texto.isAntiAlias = true

        // fundo escuro atras do texto, pra ficar legivel sobre qualquer cor
        val fundo = Paint()
        fundo.color = Color.argb(150, 0, 0, 0)
        fundo.style = Paint.Style.FILL

        for (obj in lista) {
            val caixa = obj.caixaDelimitadora
            canvas.drawRect(caixa, caneta)

            val nome = traduzir(obj.nomeObjeto)
            val larguraTexto = texto.measureText(nome)

            // posicao do texto: acima da caixa; se nao couber, joga pra dentro
            var textoX = caixa.left
            var textoY = caixa.top - 10f
            if (textoY < 34f) textoY = caixa.top + 38f

            // nao deixa o texto sair pelas laterais da tela
            if (textoX < 4f) textoX = 4f
            if (textoX + larguraTexto > bmp.width) textoX = bmp.width - larguraTexto - 4f

            // desenha o fundo e depois o nome
            canvas.drawRect(textoX - 4f, textoY - 32f, textoX + larguraTexto + 8f, textoY + 8f, fundo)
            canvas.drawText(nome, textoX, textoY, texto)
        }
        return bmp
    }

    // fala os nomes dos objetos apenas quando a lista muda (evita repetir)
    private fun anunciar(lista: List<ObjectDetection>) {
        val nomes = lista.map { traduzir(it.nomeObjeto) }.toSet()
        if (nomes.isNotEmpty() && nomes != ultimosNomes) {
            ultimosNomes = nomes
            if (vozLigada) locutor.falar(nomes.joinToString(", "))
        }
    }

    // traduz os nomes das classes do COCO (ingles) para portugues
    private fun traduzir(nome: String): String {
        return when (nome.lowercase().trim()) {
            "person" -> "pessoa"
            "bicycle" -> "bicicleta"
            "car" -> "carro"
            "motorcycle" -> "moto"
            "airplane" -> "aviao"
            "bus" -> "onibus"
            "train" -> "trem"
            "truck" -> "caminhao"
            "boat" -> "barco"
            "traffic light" -> "semaforo"
            "fire hydrant" -> "hidrante"
            "stop sign" -> "placa de pare"
            "parking meter" -> "parquimetro"
            "bench" -> "banco"
            "bird" -> "passaro"
            "cat" -> "gato"
            "dog" -> "cachorro"
            "horse" -> "cavalo"
            "sheep" -> "ovelha"
            "cow" -> "vaca"
            "elephant" -> "elefante"
            "bear" -> "urso"
            "zebra" -> "zebra"
            "giraffe" -> "girafa"
            "backpack" -> "mochila"
            "umbrella" -> "guarda-chuva"
            "handbag" -> "bolsa"
            "tie" -> "gravata"
            "suitcase" -> "mala"
            "frisbee" -> "frisbee"
            "skis" -> "esqui"
            "snowboard" -> "snowboard"
            "sports ball" -> "bola"
            "kite" -> "pipa"
            "baseball bat" -> "taco de beisebol"
            "baseball glove" -> "luva de beisebol"
            "skateboard" -> "skate"
            "surfboard" -> "prancha de surf"
            "tennis racket" -> "raquete"
            "bottle" -> "garrafa"
            "wine glass" -> "taca"
            "cup" -> "copo"
            "fork" -> "garfo"
            "knife" -> "faca"
            "spoon" -> "colher"
            "bowl" -> "tigela"
            "banana" -> "banana"
            "apple" -> "maca"
            "sandwich" -> "sanduiche"
            "orange" -> "laranja"
            "broccoli" -> "brocolis"
            "carrot" -> "cenoura"
            "hot dog" -> "cachorro-quente"
            "pizza" -> "pizza"
            "donut" -> "rosquinha"
            "cake" -> "bolo"
            "chair" -> "cadeira"
            "couch" -> "sofa"
            "potted plant" -> "planta"
            "bed" -> "cama"
            "dining table" -> "mesa"
            "toilet" -> "vaso sanitario"
            "tv" -> "televisao"
            "laptop" -> "notebook"
            "mouse" -> "mouse"
            "remote" -> "controle"
            "keyboard" -> "teclado"
            "cell phone" -> "celular"
            "microwave" -> "microondas"
            "oven" -> "forno"
            "toaster" -> "torradeira"
            "sink" -> "pia"
            "refrigerator" -> "geladeira"
            "book" -> "livro"
            "clock" -> "relogio"
            "vase" -> "vaso"
            "scissors" -> "tesoura"
            "teddy bear" -> "urso de pelucia"
            "hair drier" -> "secador"
            "toothbrush" -> "escova de dente"
            else -> nome
        }
    }

    private fun rotacionarBitmap(bitmap: Bitmap, graus: Int): Bitmap {
        if (graus == 0) return bitmap
        val matriz = Matrix()
        matriz.postRotate(graus.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
    }

    override fun onDestroy() {
        super.onDestroy()
        executorCamera.shutdown()
        locutor.encerrar()
    }
}