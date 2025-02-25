package com.example.inventCalc2

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class AddCodigoActivity : AppCompatActivity() {

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_add_codigo)

val inputCodigo: EditText = findViewById(R.id.input_codigo)
val inputDescricao: EditText = findViewById(R.id.input_descricao)
val inputValorUnitario: EditText = findViewById(R.id.input_valor_unitario)
val inputTipo: Spinner = findViewById(R.id.input_tipo)
val inputQuantidadeRafia: EditText = findViewById(R.id.input_quantidade_rafia)
val saveButton: Button = findViewById(R.id.save_button)
val cancelButton: Button = findViewById(R.id.cancel_button)

inputTipo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
val tipo = parent.getItemAtPosition(position).toString()
inputQuantidadeRafia.visibility = if (tipo == "PA" || tipo == "PI") View.VISIBLE else View.GONE
}

override fun onNothingSelected(parent: AdapterView<*>) {}
}

saveButton.setOnClickListener {
val codigo = inputCodigo.text.toString()
val descricao = inputDescricao.text.toString()
val valorUnitario = inputValorUnitario.text.toString()
val tipo = inputTipo.selectedItem.toString()
val quantidadeRafia = if (inputQuantidadeRafia.visibility == View.VISIBLE) inputQuantidadeRafia.text.toString() else ""

if (codigo.isNotEmpty() && descricao.isNotEmpty() && valorUnitario.isNotEmpty() && (tipo == "MP" || quantidadeRafia.isNotEmpty())) {
addCodigoToFile(codigo, descricao, valorUnitario, tipo, quantidadeRafia)
Toast.makeText(this, "Código adicionado com sucesso!", Toast.LENGTH_SHORT).show()
finish() // Fecha a atividade após salvar
} else {
Toast.makeText(this, "Por favor, preencha todos os campos", Toast.LENGTH_SHORT).show()
}
}

cancelButton.setOnClickListener {
finish() // Fecha a atividade ao cancelar
}
}

private fun addCodigoToFile(codigo: String, descricao: String, valorUnitario: String, tipo: String, quantidadeRafia: String) {
val file = File(filesDir, "codigos.txt")
val entry = if (tipo == "MP") "$codigo: $descricao, $valorUnitario, $tipo\n" else "$codigo: $descricao, $valorUnitario, $tipo, $quantidadeRafia\n"
file.appendText(entry)
}
}