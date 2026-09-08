package com.shohan.khatago.export

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.shohan.khatago.core.Money
import com.shohan.khatago.domain.ReportSummary

object PdfExporter {
    fun export(context: Context, uri: Uri, periodLabel: String, summary: ReportSummary) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        var y = 60f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 26f
        canvas.drawText("KhataGo", 40f, y, paint)
        y += 28f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 14f
        canvas.drawText("All your finances, in one place.", 40f, y, paint)
        y += 20f
        canvas.drawText("Created by Shohan Khan", 40f, y, paint)
        y += 16f
        canvas.drawText("helloiamshohan@gmail.com", 40f, y, paint)
        y += 28f
        canvas.drawText("Report Period: $periodLabel", 40f, y, paint)
        y += 26f

        val lines = listOf(
            "Income: ${Money.format(summary.incomeMinor)}",
            "Expense: ${Money.format(summary.expenseMinor)}",
            "Net Cash Flow: ${Money.format(summary.netCashFlowMinor, showPlus = summary.netCashFlowMinor > 0)}",
            "Payments: ${Money.format(summary.paymentMinor)}",
            "Outstanding: ${Money.format(summary.outstandingMinor)}"
        )
        paint.textSize = 16f
        lines.forEach {
            canvas.drawText(it, 40f, y, paint)
            y += 24f
        }

        y += 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Debt Distribution", 40f, y, paint)
        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        summary.debtDistribution.forEach {
            canvas.drawText("${it.label}: ${Money.format(it.amountMinor)}", 40f, y, paint)
            y += 20f
        }

        y += 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Insights", 40f, y, paint)
        y += 24f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        if (summary.insights.isEmpty()) {
            canvas.drawText("Add more financial activity to generate insights.", 40f, y, paint)
        } else {
            summary.insights.forEach {
                canvas.drawText("• $it", 40f, y, paint)
                y += 20f
            }
        }

        document.finishPage(page)
        context.contentResolver.openOutputStream(uri)?.use { output ->
            document.writeTo(output)
        }
        document.close()
    }
}
