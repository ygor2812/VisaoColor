package com.visaocolor.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
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
import com.visaocolor.models.ColorBlindnessType
import com.visaocolor.repositories.LocalStorageRepository
import com.visaocolor.services.BrettelFilterProcessor
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var executorCamera: ExecutorService
    private val processadorFiltro = BrettelFilterProcessor()
    private lateinit var armazenamento: LocalStorageRepository

    private lateinit var imagemCamera: ImageView
    private lateinit var textoStatus: TextView
    private lateinit var painelAjustes: View

    private var tipoAtual: ColorBlindnessType = ColorBlindnessType.DEUTERANOPIA
    private var filtroLigado = true

    private var brilho = 0
    private var contraste = 100
    private var intensidade = 100

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
        painelAjustes = findViewById(R.id.painelAjustes)

        armazenamento = LocalStorageRepository(applicationContext)
        executorCamera = Executors.newSingleThreadExecutor()

        carregarConfiguracoes()
        configurarControles()
        configurarSliders()
        solicitarPermissaoCamera()
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
            brilho = 0
            contraste = 100
            intensidade = 100
            findViewById<SeekBar>(R.id.sliderBrilho).progress = 100
            findViewById<SeekBar>(R.id.sliderContraste).progress = 100
            findViewById<SeekBar>(R.id.sliderIntensidade).progress = 100
            atualizarLabels()
            salvarConfiguracoes()
        }

        atualizarStatus()
    }

    private fun configurarSliders() {
        findViewById<SeekBar>(R.id.sliderBrilho).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    brilho = valor - 100
                    atualizarLabels()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { salvarConfiguracoes() }
            })

        findViewById<SeekBar>(R.id.sliderContraste).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    contraste = valor
                    atualizarLabels()
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) { salvarConfiguracoes() }
            })

        findViewById<SeekBar>(R.id.sliderIntensidade).setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, valor: Int, user: Boolean) {
                    intensidade = valor
                    atualizarLabels()
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
        val bitmap = rotacionarBitmap(imagem.toBitmap(), rotacao)

        val tipoUsar = if (filtroLigado) tipoAtual else ColorBlindnessType.NONE
        val bitmapFinal = processadorFiltro.aplicar(bitmap, tipoUsar, brilho, contraste, intensidade)

        runOnUiThread { imagemCamera.setImageBitmap(bitmapFinal) }
        imagem.close()
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
    }
}