package com.example.background.workers

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.background.KEY_IMAGE_URI
import com.example.background.R

class BlurWorker(ctx: Context, workerParams: WorkerParameters): Worker(ctx, workerParams) {

    override fun doWork(): Result {
        val appContext = applicationContext

        val resourceUri = inputData.getString(KEY_IMAGE_URI) // чтобы получить URI, который мы передали из Data

        makeStatusNotification("Blurring image", appContext)

        return try {
//            val picture = BitmapFactory.decodeResource( // прочитать про битмап фабрику
//                appContext.resources, // получение ресурса изображения
//                R.drawable.android_cupcake)

            if (TextUtils.isEmpty(resourceUri)) { // проверка полученного URI на наличие контента
                Log.e(TAG, "Invalid input uri")
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver

            val picture = BitmapFactory.decodeStream(
                appContext.contentResolver.openInputStream(Uri.parse(resourceUri))) // получение самого изображения

            val output = blurBitmap(picture, appContext) // блюрим картинку и записываем в локальную переменную

            // Записать растровое изображение во временный файл
            val outputUri = writeBitmapToFile(applicationContext, picture) // записываем результат в файл

            val outputData = workDataOf(KEY_IMAGE_URI to outputUri.toString()) // выходной URI

            makeStatusNotification("Output is $outputUri", appContext) // уведомление, отображающее URI
            Result.success(outputData)
        } catch(throwable: Throwable) {
            Log.e(TAG, "Error applying blur")
            return Result.failure()
        }
    }
}