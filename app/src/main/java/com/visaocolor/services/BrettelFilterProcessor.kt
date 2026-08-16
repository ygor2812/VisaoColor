package com.visaocolor.services

import android.graphics.Bitmap
import com.visaocolor.models.ColorBlindnessType
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

class BrettelFilterProcessor {

    companion object {
        init {
            System.loadLibrary("opencv_java4")
        }
    }

    private val filterService = ChromaticFilterService()

    fun aplicar(origem: Bitmap, tipo: ColorBlindnessType): Bitmap {
        if (tipo == ColorBlindnessType.NONE) {
            return origem
        }

        val matRgba = Mat()
        Utils.bitmapToMat(origem, matRgba)

        val matRgb = Mat()
        Imgproc.cvtColor(matRgba, matRgb, Imgproc.COLOR_RGBA2RGB)

        val valores = filterService.obterMatrizPara(tipo)
        val matrizBrettel = Mat(3, 3, CvType.CV_32F)
        matrizBrettel.put(0, 0, floatArrayOf(
            valores[0], valores[1], valores[2],
            valores[3], valores[4], valores[5],
            valores[6], valores[7], valores[8]
        ))

        val matFiltrado = Mat()
        Core.transform(matRgb, matFiltrado, matrizBrettel)

        val matSaida = Mat()
        Imgproc.cvtColor(matFiltrado, matSaida, Imgproc.COLOR_RGB2RGBA)

        val bitmapSaida = Bitmap.createBitmap(
            origem.width, origem.height, Bitmap.Config.ARGB_8888
        )
        Utils.matToBitmap(matSaida, bitmapSaida)

        matRgba.release()
        matRgb.release()
        matFiltrado.release()
        matSaida.release()
        matrizBrettel.release()

        return bitmapSaida
    }
}