package com.example.purplepaysimulator

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputType
import androidx.activity.ComponentActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val purple = Color.rgb(83, 35, 145)
    private val deep = Color.rgb(43, 18, 76)
    private val prefs by lazy { getSharedPreferences("practice_history", MODE_PRIVATE) }
    private lateinit var root: LinearLayout
    private lateinit var nameInput: EditText
    private lateinit var upiInput: EditText
    private lateinit var amountInput: EditText
    private lateinit var noteInput: EditText

    private val scanLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents != null) {
            val data = parseUpiQr(result.contents)
            nameInput.setText(data["pn"] ?: "")
            upiInput.setText(data["pa"] ?: "")
            if (!data["am"].isNullOrBlank()) amountInput.setText(data["am"])
            Toast.makeText(this, "QR details read. Verify them before continuing.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun text(value: String, size: Float, color: Int = Color.BLACK, bold: Boolean = false): TextView {
        return TextView(this).apply {
            text = value
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(null, Typeface.BOLD)
            setPadding(0, 8, 0, 8)
        }
    }

    private fun button(label: String, action: () -> Unit): Button =
        Button(this).apply { text = label; setOnClickListener { action() } }

    private fun field(hint: String, numeric: Boolean = false): EditText =
        EditText(this).apply {
            this.hint = hint
            textSize = 16f
            setSingleLine(true)
            if (numeric) inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(24, 12, 24, 12)
            setBackgroundColor(Color.rgb(247, 244, 251))
        }

    private fun page(title: String): LinearLayout {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.rgb(250, 248, 253))
            setPadding(22, 22, 22, 22)
        }
        val header = TextView(this).apply {
            text = "PURPLEPAY SIMULATOR"
            textSize = 13f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setPadding(16, 15, 16, 15)
            setBackgroundColor(deep)
        }
        root.addView(header)
        root.addView(text(title, 25f, deep, true))
        root.addView(text("PRACTICE MODE • NO MONEY TRANSFERRED", 12f, Color.rgb(180, 35, 35), true))
        val scroll = ScrollView(this)
        val inner = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(0, 10, 0, 18) }
        scroll.addView(inner)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        content = inner
        return inner
    }

    private lateinit var content: LinearLayout

    private fun showHome() {
        val box = page("Send practice payment")
        box.addView(text("Try a QR code or enter recipient details manually. This app never sends UPI payments.", 15f))
        box.addView(button("SCAN UPI QR CODE") { scanQr() })
        box.addView(text("Recipient name", 14f, deep, true))
        nameInput = field("Enter recipient name")
        box.addView(nameInput)
        box.addView(text("UPI ID (optional)", 14f, deep, true))
        upiInput = field("example@upi")
        box.addView(upiInput)
        box.addView(text("Amount (₹)", 14f, deep, true))
        amountInput = field("Enter amount", true)
        box.addView(amountInput)
        box.addView(text("Note (optional)", 14f, deep, true))
        noteInput = field("What is this practice payment for?")
        box.addView(noteInput)
        box.addView(button("REVIEW PRACTICE PAYMENT") { review() })
        box.addView(button("VIEW PRACTICE HISTORY") { showHistory() })
    }

    private fun scanQr() {
        val options = ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            .setPrompt("Scan a UPI QR code — practice mode only")
            .setBeepEnabled(false).setOrientationLocked(false)
        scanLauncher.launch(options)
    }

    private fun parseUpiQr(raw: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            val query = raw.substringAfter("?", "")
            query.split("&").forEach {
                val parts = it.split("=", limit = 2)
                if (parts.size == 2) result[URLDecoder.decode(parts[0], "UTF-8")] =
                    URLDecoder.decode(parts[1], "UTF-8")
            }
        } catch (_: Exception) { }
        return result
    }

    private fun review() {
        val name = nameInput.text.toString().trim()
        val amount = amountInput.text.toString().toDoubleOrNull()
        if (name.isBlank() || amount == null || amount <= 0) {
            Toast.makeText(this, "Enter a recipient name and a valid amount.", Toast.LENGTH_LONG).show()
            return
        }
        val box = page("Review practice payment")
        box.addView(text("Recipient: $name", 18f, deep, true))
        box.addView(text("UPI ID: ${upiInput.text}", 15f))
        box.addView(text("Amount: ₹${"%.2f".format(Locale.US, amount)}", 23f, deep, true))
        box.addView(text("This is a simulation. No bank, UPI app, or payment network is contacted.", 15f))
        box.addView(button("CONFIRM SIMULATION") { saveAndShow(name, amount) })
        box.addView(button("BACK") { showHome() })
    }

    private fun saveAndShow(name: String, amount: Double) {
        val stamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val entry = JSONObject().put("name", name).put("amount", amount)
            .put("upi", upiInput.text.toString()).put("time", stamp)
        val list = try { JSONArray(prefs.getString("items", "[]")) } catch (_: Exception) { JSONArray() }
        list.put(entry)
        prefs.edit().putString("items", list.toString()).apply()
        val box = page("Practice complete")
        box.gravity = Gravity.CENTER_HORIZONTAL
        box.addView(text("✓", 54f, purple, true))
        box.addView(text("SIMULATION COMPLETED", 23f, deep, true))
        box.addView(text("₹${"%.2f".format(Locale.US, amount)}", 32f, deep, true))
        box.addView(text("To: $name\n$stamp", 16f))
        box.addView(text("NOT A REAL PAYMENT\nNo money was transferred. This confirmation is for the practice game only.", 16f, Color.rgb(170, 25, 25), true))
        box.addView(button("GO TO HOME") { showHome() })
        box.addView(button("VIEW PRACTICE HISTORY") { showHistory() })
    }

    private fun showHistory() {
        val box = page("Practice history")
        val list = try { JSONArray(prefs.getString("items", "[]")) } catch (_: Exception) { JSONArray() }
        if (list.length() == 0) box.addView(text("No practice payments recorded yet.", 16f))
        for (i in list.length() - 1 downTo 0) {
            val item = list.getJSONObject(i)
            box.addView(text("${item.optString("name")}  •  ₹${"%.2f".format(Locale.US, item.optDouble("amount"))}\n${item.optString("time")}\nSIMULATED — NOT PAID", 16f, deep, true))
        }
        box.addView(button("CLEAR PRACTICE HISTORY") {
            prefs.edit().remove("items").apply()
            showHistory()
        })
        box.addView(button("BACK HOME") { showHome() })
    }
}
