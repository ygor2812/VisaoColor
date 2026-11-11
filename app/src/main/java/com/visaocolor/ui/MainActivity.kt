package com.visaocolor.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.visaocolor.R
import com.visaocolor.controllers.ColorIdentificationController
import com.visaocolor.controllers.StartupController
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.models.ObjectDetection
import com.visaocolor.repositories.LocalStorageRepository
import com.visaocolor.repositories.SessionColorRepository
import com.visaocolor.repositories.SettingsRepository
import com.visaocolor.services.BrettelFilterProcessor
import com.visaocolor.services.ColorAnalyzer
import com.visaocolor.services.LightingMonitor
import com.visaocolor.services.ObjectDetectionEngine
import com.visaocolor.services.SpeechSynthesizer
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        const val TEXTO_TERMOS =
            "O VisaoColor e uma ferramenta de auxilio e nao substitui avaliacao medica. " +
                    "As cores e objetos identificados sao aproximacoes e podem variar conforme a " +
                    "iluminacao e a camera. Nao use o aplicativo como unica referencia em situacoes " +
                    "criticas, como transito, medicacao ou seguranca."
    }

    private lateinit var executorCamera: ExecutorService
    private val processadorFiltro = BrettelFilterProcessor()
    private lateinit var armazenamento: LocalStorageRepository
    private lateinit var preferencias: SettingsRepository

    // identificacao de cor por toque (Modulo 3)
    private val controladorCor = ColorIdentificationController(
        ColorAnalyzer(),
        SessionColorRepository()
    )

    // IA e voz (Modulo 4)
    private lateinit var motorIA: ObjectDetectionEngine
    private lateinit var locutor: SpeechSynthesizer

    // iluminacao e inicializacao (Modulo 5)
    private val monitorLuz = LightingMonitor()
    private lateinit var inicializacao: StartupController

    private lateinit var imagemCamera: ImageView
    private lateinit var textoStatus: TextView
    private lateinit var textoCor: TextView
    private lateinit var textoAlerta: TextView
    private lateinit var marcador: View
    private lateinit var painelAjustes: View
    private lateinit var sliderZoom: SeekBar
    private lateinit var botaoSensibilidade: Button

    private val anelMarcador = GradientDrawable()

    @Volatile private var ultimoOriginal: Bitmap? = null

    private var tipoAtual: ColorBlindnessType = ColorBlindnessType.DEUTERANOPIA
    private var filtroLigado = true

    private var brilho = 0
    private var contraste = 100
    private var intensidade = 100

    private var iaLigada = false
    private var vozLigada = false
    @Volatile private var deteccoes: List<ObjectDetection> = emptyList()
    private var ultimaDeteccaoMs = 0L
    private var ultimosNomes: Set<String> = emptySet()

    private var ultimaLuzMs = 0L
    private var ultimoStatusLuz = LightingMonitor.Status.ADEQUADO

    private lateinit var detectorEscala: ScaleGestureDetector
    private var cameraControle: CameraControl? = null
    private var zoomAtual = 1f
    private var houveZoom = false

    private val manipulador = Handler(Looper.getMainLooper())
    private val esconderCor = Runnable {
        textoCor.visibility = View.GONE
        marcador.visibility = View.GONE
    }

    // tutorial dividido em etapas (RF 37)
    private val etapasTutorial = listOf(
        Pair("Tutorial 1 de 4 - Correcao de cores",
            "Escolha o perfil que corresponde ao seu tipo de daltonismo:\n\n" +
                    "Protan - dificuldade com o vermelho\n" +
                    "Deuter - dificuldade com o verde\n" +
                    "Tritan - dificuldade com o azul\n\n" +
                    "O botao Filtro liga e desliga a correcao, permitindo comparar com a imagem original."),
        Pair("Tutorial 2 de 4 - Identificar cores",
            "Toque com um dedo em qualquer ponto da tela.\n\n" +
                    "O nome da cor aparece no alto e um marcador preenchido com a cor indica o ponto tocado. " +
                    "O aviso desaparece sozinho apos tres segundos.\n\n" +
                    "O botao Historico mostra todas as cores identificadas na sessao."),
        Pair("Tutorial 3 de 4 - Objetos e voz",
            "O botao IA liga o reconhecimento de objetos. Os objetos aparecem dentro de um " +
                    "retangulo verde, com o nome e o grau de confianca.\n\n" +
                    "O botao Voz faz o aplicativo falar os nomes das cores e dos objetos. " +
                    "A velocidade e o volume podem ser ajustados em Ajustes."),
        Pair("Tutorial 4 de 4 - Zoom e alertas",
            "Use dois dedos sobre a imagem para ampliar ate cinco vezes, ou o controle de " +
                    "zoom em Ajustes.\n\n" +
                    "Quando a iluminacao estiver inadequada, um aviso vermelho aparece na parte " +
                    "inferior da tela. A sensibilidade desse aviso pode ser alterada em Ajustes.")
    )

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
        textoAlerta = findViewById(R.id.textoAlerta)
        marcador = findViewById(R.id.marcador)
        painelAjustes = findViewById(R.id.painelAjustes)
        sliderZoom = findViewById(R.id.sliderZoom)
        botaoSensibilidade = findViewById(R.id.botaoSensibilidade)

        armazenamento = LocalStorageRepository(applicationContext)
        preferencias = SettingsRepository(applicationContext)
        inicializacao = StartupController(armazenamento)
        executorCamera = Executors.newSingleThreadExecutor()

        motorIA = ObjectDetectionEngine(applicationContext)
        locutor = SpeechSynthesizer(applicationContext)
        locutor.iniciar()
        Thread {
            try { motorIA.carregar() } catch (e: Exception) {
                Log.e("VisaoColor", "Falha ao carregar modelo de IA", e)
            }
        }.start()

        prepararMarcador()
        prepararZoom()
        configurarToque()
        carregarConfiguracoes()
        configurarControles()
        configurarSliders()
        verificarInicializacao()
        solicitarPermissaoCamera()
    }

    private fun prepararMarcador() {
        anelMarcador.shape = GradientDrawable.OVAL
        anelMarcador.setColor(Color.TRANSPARENT)
        anelMarcador.setStroke(8, Color.WHITE)
        marcador.background = anelMarcador
    }

    private fun prepararZoom() {
        detectorEscala = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    houveZoom = true
                    aplicarZoom(zoomAtual * detector.scaleFactor, true)
                    return true
                }
            })
    }

    // aplica o zoom e mantem o controle deslizante sincronizado (RF 32, 33, 34)
    private fun aplicarZoom(valor: Float, atualizarSlider: Boolean) {
        zoomAtual = valor.coerceIn(1f, 5f)
        cameraControle?.setZoomRatio(zoomAtual)
        findViewById<TextView>(R.id.labelZoom).text =
            "Zoom: " + String.format("%.1f", zoomAtual) + "x"
        if (atualizarSlider) {
            sliderZoom.progress = (((zoomAtual - 1f) / 4f) * 100).toInt()
        }
    }

    private fun configurarToque() {
        imagemCamera.setOnTouchListener { v, event ->
            detectorEscala.onTouchEvent(event)
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> houveZoom = false
                MotionEvent.ACTION_UP -> {
                    if (!houveZoom && event.pointerCount == 1) {
                        identificarPelaTela(event.x, event.y)
                        v.performClick()
                    }
                }
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

        textoCor.text = registro.nome
        textoCor.visibility = View.VISIBLE

        // marcador preenchido com a cor identificada (RF 19)
        anelMarcador.setColor(pixel)
        marcador.translationX = tocX - marcador.width / 2f
        marcador.translationY = tocY - marcador.height / 2f
        marcador.visibility = View.VISIBLE

        // le a cor em voz alta (RF 24)
        if (vozLigada) locutor.falar(registro.nome)

        manipulador.removeCallbacks(esconderCor)
        manipulador.postDelayed(esconderCor, 3000)
    }

    private fun verificarInicializacao() = lifecycleScope.launch {
        if (!inicializacao.aceitouTermos()) {
            mostrarTermos(true)
        } else if (inicializacao.ehPrimeiroAcesso()) {
            mostrarTutorial(0)
        }
    }

    // RF 38, 39: no primeiro acesso exige aceite; depois serve apenas para consulta
    private fun mostrarTermos(primeiroAcesso: Boolean) {
        val construtor = AlertDialog.Builder(this)
            .setTitle("Termos de Uso e Seguranca")
            .setMessage(TEXTO_TERMOS)

        if (primeiroAcesso) {
            construtor.setCancelable(false)
            construtor.setPositiveButton("Aceitar e continuar") { _, _ ->
                lifecycleScope.launch {
                    inicializacao.aceitarTermos()
                    if (inicializacao.ehPrimeiroAcesso()) mostrarTutorial(0)
                }
            }
            construtor.setNegativeButton("Recusar") { _, _ -> finish() }
        } else {
            construtor.setPositiveButton("Fechar", null)
            construtor.setNeutralButton("Compartilhar") { _, _ -> compartilharTermos() }
        }
        construtor.show()
    }

    // RF 40: envia os termos por outro aplicativo do dispositivo
    private fun compartilharTermos() {
        val intencao = Intent(Intent.ACTION_SEND)
        intencao.type = "text/plain"
        intencao.putExtra(Intent.EXTRA_SUBJECT, "Termos de Uso - VisaoColor")
        intencao.putExtra(Intent.EXTRA_TEXT, TEXTO_TERMOS)
        startActivity(Intent.createChooser(intencao, "Compartilhar os termos"))
    }

    // RF 35, 36, 37: tutorial em etapas, com avanco e retorno
    private fun mostrarTutorial(etapa: Int) {
        val (titulo, texto) = etapasTutorial[etapa]
        val construtor = AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(texto)
            .setCancelable(false)

        if (etapa < etapasTutorial.size - 1) {
            construtor.setPositiveButton("Proximo") { _, _ -> mostrarTutorial(etapa + 1) }
        } else {
            construtor.setPositiveButton("Concluir") { _, _ ->
                lifecycleScope.launch { inicializacao.concluirTutorial() }
            }
        }
        if (etapa > 0) {
            construtor.setNegativeButton("Voltar") { _, _ -> mostrarTutorial(etapa - 1) }
        }
        construtor.show()
    }

    private fun mostrarAjuda() {
        val opcoes = arrayOf("Ver o tutorial", "Ler os termos de uso", "Compartilhar os termos")
        AlertDialog.Builder(this)
            .setTitle("Ajuda")
            .setItems(opcoes) { _, indice ->
                when (indice) {
                    0 -> mostrarTutorial(0)
                    1 -> mostrarTermos(false)
                    2 -> compartilharTermos()
                }
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    // RF 20: historico de cores da sessao
    private fun mostrarHistorico() {
        val historico = controladorCor.obterHistorico()
        if (historico.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Historico de cores")
                .setMessage("Nenhuma cor foi identificada nesta sessao. Toque na tela para identificar uma cor.")
                .setPositiveButton("Fechar", null)
                .show()
            return
        }
        val itens = historico.reversed()
            .map { it.horarioFormatado() + "   " + it.nome + "   " + it.paraHex() }
            .toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Historico de cores (" + historico.size + ")")
            .setItems(itens, null)
            .setNegativeButton("Limpar") { _, _ -> controladorCor.limparHistorico() }
            .setPositiveButton("Fechar", null)
            .show()
    }

    private fun carregarConfiguracoes() = lifecycleScope.launch {
        val config = armazenamento.obterConfiguracoesImagem()
        brilho = config.brilho
        contraste = config.contraste
        intensidade = config.intensidade

        findViewById<SeekBar>(R.id.sliderBrilho).progress = brilho + 100
        findViewById<SeekBar>(R.id.sliderContraste).progress = contraste
        findViewById<SeekBar>(R.id.sliderIntensidade).progress = intensidade

        // perfil de daltonismo salvo (RF 13)
        val perfilSalvo = armazenamento.obterPerfil().first()
        if (perfilSalvo != ColorBlindnessType.NONE) tipoAtual = perfilSalvo

        // sensibilidade dos alertas (RF 31)
        val niveis = LightingMonitor.Sensibilidade.values()
        val indice = preferencias.obterSensibilidade().coerceIn(0, niveis.size - 1)
        monitorLuz.sensibilidade = niveis[indice]
        botaoSensibilidade.text = "Sensibilidade dos alertas: " + niveis[indice].rotulo

        // voz (RF 26)
        val velocidade = preferencias.obterVelocidadeVoz()
        val volume = preferencias.obterVolumeVoz()
        locutor.definirVelocidade(velocidade)
        locutor.definirVolume(volume)
        findViewById<SeekBar>(R.id.sliderVelocidadeVoz).progress =
            (((velocidade - 0.5f) / 1.5f) * 100).toInt()
        findViewById<SeekBar>(R.id.sliderVolumeVoz).progress = (volume * 100).toInt()
        findViewById<TextView>(R.id.labelVelocidadeVoz).text =
            "Velocidade da voz: " + String.format("%.1f", velocidade) + "x"
        findViewById<TextView>(R.id.labelVolumeVoz).text =
            "Volume da voz: " + (volume * 100).toInt() + "%"

        atualizarLabels()
        atualizarStatus()
    }

    private fun configurarControles() {
        findViewById<Button>(R.id.botaoProtanopia).setOnClickListener {
            selecionarPerfil(ColorBlindnessType.PROTANOPIA)
        }
        findViewById<Button>(R.id.botaoDeuteranopia).setOnClickListener {
            selecionarPerfil(ColorBlindnessType.DEUTERANOPIA)
        }
        findViewById<Button>(R.id.botaoTritanopia).setOnClickListener {
            selecionarPerfil(ColorBlindnessType.TRITANOPIA)
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
            aplicarZoom(1f, true)
            atualizarLabels(); salvarConfiguracoes()
        }

        // alterna entre Baixa, Media e Alta (RF 31)
        botaoSensibilidade.setOnClickListener {
            val niveis = LightingMonitor.Sensibilidade.values()
            val proximo = (monitorLuz.sensibilidade.ordinal + 1) % niveis.size
            monitorLuz.sensibilidade = niveis[proximo]
            botaoSensibilidade.text = "Sensibilidade dos alertas: " + niveis[proximo].rotulo
            lifecycleScope.launch { preferencias.salvarSensibilidade(proximo) }
        }

        val botaoIA = findViewById<Button>(R.id.botaoIA)
        botaoIA.setOnClickListener {
            iaLigada = !iaLigada
            botaoIA.text = if (iaLigada) "IA: LIGADA" else "IA: OFF"
            if (!iaLigada) deteccoes = emptyList()
        }

        val botaoVoz = findViewById<Button>(R.id.botaoVoz)
        botaoVoz.setOnClickListener {
            vozLigada = !vozLigada
            locutor.definirAtivo(vozLigada)
            botaoVoz.text = if (vozLigada) "Voz: LIGADA" else "Voz: OFF"
        }

        findViewById<Button>(R.id.botaoHistorico).setOnClickListener { mostrarHistorico() }
        findViewById<Button>(R.id.botaoAjuda).setOnClickListener { mostrarAjuda() }

        atualizarStatus()
    }

    // RF 10, 13: aplica e salva o perfil escolhido
    private fun selecionarPerfil(tipo: ColorBlindnessType) {
        tipoAtual = tipo
        atualizarStatus()
        lifecycleScope.launch { armazenamento.salvarPerfil(tipo) }
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

        // controle deslizante de zoom (RF 34)
        sliderZoom.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    if (user) aplicarZoom(1f + (valor / 100f) * 4f, false)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

        // velocidade da voz (RF 26)
        findViewById<SeekBar>(R.id.sliderVelocidadeVoz).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    val velocidade = 0.5f + (valor / 100f) * 1.5f
                    locutor.definirVelocidade(velocidade)
                    findViewById<TextView>(R.id.labelVelocidadeVoz).text =
                        "Velocidade da voz: " + String.format("%.1f", velocidade) + "x"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {
                    val velocidade = 0.5f + ((sb?.progress ?: 33) / 100f) * 1.5f
                    lifecycleScope.launch { preferencias.salvarVelocidadeVoz(velocidade) }
                    if (vozLigada) locutor.falar("Velocidade da voz ajustada")
                }
            })

        // volume da voz (RF 26)
        findViewById<SeekBar>(R.id.sliderVolumeVoz).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    locutor.definirVolume(valor / 100f)
                    findViewById<TextView>(R.id.labelVolumeVoz).text = "Volume da voz: " + valor + "%"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {
                    val volume = (sb?.progress ?: 100) / 100f
                    lifecycleScope.launch { preferencias.salvarVolumeVoz(volume) }
                    if (vozLigada) locutor.falar("Volume ajustado")
                }
            })
    }

    private fun atualizarLabels() {
        findViewById<TextView>(R.id.labelBrilho).text = "Brilho: " + brilho
        findViewById<TextView>(R.id.labelContraste).text = "Contraste: " + contraste
        findViewById<TextView>(R.id.labelIntensidade).text = "Intensidade: " + intensidade
    }

    private fun salvarConfiguracoes() = lifecycleScope.launch {
        armazenamento.salvarBrilho(brilho)
        armazenamento.salvarContraste(contraste)
        armazenamento.salvarIntensidade(intensidade)
    }

    private fun atualizarStatus() {
        val nomeFiltro = if (filtroLigado) tipoAtual.nomeExibicao else "Original"
        textoStatus.text = "Perfil: " + nomeFiltro
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
                val camera = provedor.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, analise
                )
                cameraControle = camera.cameraControl
            } catch (e: Exception) {
                Log.e("VisaoColor", "Erro ao iniciar a camera", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun processarFrame(imagem: ImageProxy) {
        val rotacao = imagem.imageInfo.rotationDegrees
        val original = rotacionarBitmap(imagem.toBitmap(), rotacao)
        ultimoOriginal = original

        val agora = System.currentTimeMillis()

        if (agora - ultimaLuzMs > 700) {
            ultimaLuzMs = agora
            verificarIluminacao(original)
        }

        if (iaLigada && agora - ultimaDeteccaoMs > 600) {
            ultimaDeteccaoMs = agora
            try {
                deteccoes = motorIA.detectar(original)
                anunciar(deteccoes)
            } catch (e: Exception) {
                Log.e("VisaoColor", "Erro na deteccao", e)
            }
        }

        val tipoUsar = if (filtroLigado) tipoAtual else ColorBlindnessType.NONE
        var bitmapFinal = processadorFiltro.aplicar(original, tipoUsar, brilho, contraste, intensidade)

        if (iaLigada && deteccoes.isNotEmpty()) {
            bitmapFinal = desenharCaixas(bitmapFinal, deteccoes)
        }

        runOnUiThread { imagemCamera.setImageBitmap(bitmapFinal) }
        imagem.close()
    }

    // RF 28, 29, 30: alerta visual e sonoro de iluminacao inadequada
    private fun verificarIluminacao(bmp: Bitmap) {
        val pequeno = Bitmap.createScaledBitmap(bmp, 12, 12, true)
        val pixels = mutableListOf<IntArray>()
        for (y in 0 until pequeno.height) {
            for (x in 0 until pequeno.width) {
                val pix = pequeno.getPixel(x, y)
                pixels.add(intArrayOf(Color.red(pix), Color.green(pix), Color.blue(pix)))
            }
        }
        val luz = monitorLuz.luminanciaMedia(pixels)
        val status = monitorLuz.verificarStatus(luz)

        runOnUiThread {
            when (status) {
                LightingMonitor.Status.ESCURO -> {
                    textoAlerta.text = "Ambiente escuro - as cores podem estar imprecisas"
                    textoAlerta.visibility = View.VISIBLE
                }
                LightingMonitor.Status.CLARO -> {
                    textoAlerta.text = "Muita luz - as cores podem estar imprecisas"
                    textoAlerta.visibility = View.VISIBLE
                }
                else -> textoAlerta.visibility = View.GONE
            }

            // fala o alerta apenas quando a condicao muda, para nao repetir
            if (status != ultimoStatusLuz) {
                ultimoStatusLuz = status
                if (vozLigada) {
                    when (status) {
                        LightingMonitor.Status.ESCURO ->
                            locutor.falar("Ambiente escuro. As cores podem estar imprecisas.")
                        LightingMonitor.Status.CLARO ->
                            locutor.falar("Muita luz. As cores podem estar imprecisas.")
                        else -> { }
                    }
                }
            }
        }
    }

    private fun desenharCaixas(base: Bitmap, lista: List<ObjectDetection>): Bitmap {
        val bmp = base.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bmp)

        val caneta = Paint()
        caneta.color = Color.GREEN
        caneta.style = Paint.Style.STROKE
        caneta.strokeWidth = 3f
        caneta.isAntiAlias = true

        val texto = Paint()
        texto.color = Color.GREEN
        texto.textSize = 34f
        texto.style = Paint.Style.FILL
        texto.isAntiAlias = true

        val fundo = Paint()
        fundo.color = Color.argb(150, 0, 0, 0)
        fundo.style = Paint.Style.FILL

        for (obj in lista) {
            val caixa = obj.caixaDelimitadora
            canvas.drawRect(caixa, caneta)

            // nome e nivel de confianca (RF 22)
            val confianca = (obj.confianca * 100).toInt()
            val nome = traduzir(obj.nomeObjeto) + " " + confianca + "%"
            val larguraTexto = texto.measureText(nome)

            var textoX = caixa.left
            var textoY = caixa.top - 10f
            if (textoY < 34f) textoY = caixa.top + 38f
            if (textoX < 4f) textoX = 4f
            if (textoX + larguraTexto > bmp.width) textoX = bmp.width - larguraTexto - 4f

            canvas.drawRect(textoX - 4f, textoY - 32f, textoX + larguraTexto + 8f, textoY + 8f, fundo)
            canvas.drawText(nome, textoX, textoY, texto)
        }
        return bmp
    }

    private fun anunciar(lista: List<ObjectDetection>) {
        val nomes = lista.map { traduzir(it.nomeObjeto) }.toSet()
        if (nomes.isNotEmpty() && nomes != ultimosNomes) {
            ultimosNomes = nomes
            if (vozLigada) locutor.falar(nomes.joinToString(", "))
        }
    }

    private fun traduzir(nome: String): String {
        return when (nome.lowercase().trim()) {
            "person" -> "pessoa"; "bicycle" -> "bicicleta"; "car" -> "carro"
            "motorcycle" -> "moto"; "airplane" -> "aviao"; "bus" -> "onibus"
            "train" -> "trem"; "truck" -> "caminhao"; "boat" -> "barco"
            "traffic light" -> "semaforo"; "fire hydrant" -> "hidrante"
            "stop sign" -> "placa de pare"; "parking meter" -> "parquimetro"
            "bench" -> "banco"; "bird" -> "passaro"; "cat" -> "gato"
            "dog" -> "cachorro"; "horse" -> "cavalo"; "sheep" -> "ovelha"
            "cow" -> "vaca"; "elephant" -> "elefante"; "bear" -> "urso"
            "zebra" -> "zebra"; "giraffe" -> "girafa"; "backpack" -> "mochila"
            "umbrella" -> "guarda-chuva"; "handbag" -> "bolsa"; "tie" -> "gravata"
            "suitcase" -> "mala"; "frisbee" -> "frisbee"; "skis" -> "esqui"
            "snowboard" -> "snowboard"; "sports ball" -> "bola"; "kite" -> "pipa"
            "baseball bat" -> "taco de beisebol"; "baseball glove" -> "luva de beisebol"
            "skateboard" -> "skate"; "surfboard" -> "prancha de surf"
            "tennis racket" -> "raquete"; "bottle" -> "garrafa"; "wine glass" -> "taca"
            "cup" -> "copo"; "fork" -> "garfo"; "knife" -> "faca"; "spoon" -> "colher"
            "bowl" -> "tigela"; "banana" -> "banana"; "apple" -> "maca"
            "sandwich" -> "sanduiche"; "orange" -> "laranja"; "broccoli" -> "brocolis"
            "carrot" -> "cenoura"; "hot dog" -> "cachorro-quente"; "pizza" -> "pizza"
            "donut" -> "rosquinha"; "cake" -> "bolo"; "chair" -> "cadeira"
            "couch" -> "sofa"; "potted plant" -> "planta"; "bed" -> "cama"
            "dining table" -> "mesa"; "toilet" -> "vaso sanitario"; "tv" -> "televisao"
            "laptop" -> "notebook"; "mouse" -> "mouse"; "remote" -> "controle"
            "keyboard" -> "teclado"; "cell phone" -> "celular"; "microwave" -> "microondas"
            "oven" -> "forno"; "toaster" -> "torradeira"; "sink" -> "pia"
            "refrigerator" -> "geladeira"; "book" -> "livro"; "clock" -> "relogio"
            "vase" -> "vaso"; "scissors" -> "tesoura"; "teddy bear" -> "urso de pelucia"
            "hair drier" -> "secador"; "toothbrush" -> "escova de dente"
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
        manipulador.removeCallbacks(esconderCor)
        executorCamera.shutdown()
        locutor.encerrar()
    }
}