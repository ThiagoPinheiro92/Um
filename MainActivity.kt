package com.example.inventCalc2

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
private lateinit var textArea: TextView
private lateinit var spinner: Spinner
private val pesos = mutableListOf<Double>()
private val historico = mutableListOf<String>()
private val resultados = mutableMapOf<String, Double>()
private val pigmentos = mutableMapOf<String, MutableMap<String, Double>>()
private var totalPigmentoBranco = 0.0
private var totalPigmentoPreto = 0.0
private var totalPigmentoAmarelo = 0.0
private var totalPigmentoVerde = 0.0
private var totalPigmentoVermelho = 0.0
private var totalNatural = 0.0
private var codigoMaterial: String = ""
private var tipoMaterial: String = ""
private var quantidadeRafia: Double = 0.0
private var valorUnitario: Double = 0.0
private var unidades: Int = 0

override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)

textArea = findViewById(R.id.text_area)
spinner = findViewById(R.id.codigo_spinner)
val addCodigoButton: Button = findViewById(R.id.add_codigo_button)
val removeCodigoButton: Button = findViewById(R.id.remove_codigo_button)

// Carregar códigos
createDefaultCodigosFile()
loadCodigosFromFile()

// Configurar botões
findViewById<View>(R.id.n_button).setOnClickListener { showInputDialog("N") }
findViewById<View>(R.id.b_button).setOnClickListener { showInputDialog("B") }
findViewById<View>(R.id.p_button).setOnClickListener { showInputDialog("P") }
findViewById<View>(R.id.y_button).setOnClickListener { showInputDialog("Y") }
findViewById<View>(R.id.g_button).setOnClickListener { showInputDialog("G") }
findViewById<View>(R.id.r_button).setOnClickListener { showInputDialog("R") }
findViewById<View>(R.id.s_button).setOnClickListener { calculateTotal() }
findViewById<View>(R.id.clear_button).setOnClickListener { clearCalculations() }
findViewById<View>(R.id.history_button).setOnClickListener { showHistoryDialog() }

// Configuração do botão de adicionar código
addCodigoButton.setOnClickListener {
val intent = Intent(this, AddCodigoActivity::class.java)
startActivity(intent)
}

// Configuração do botão de remover código
removeCodigoButton.setOnClickListener {
showRemoveCodigoDialog()
}
}

private fun showRemoveCodigoDialog() {
val builder = AlertDialog.Builder(this)
builder.setTitle("Remover Código")

val input = EditText(this)
input.hint = "Digite o código do material"

val scrollView = ScrollView(this)
scrollView.addView(input)
builder.setView(scrollView)

builder.setPositiveButton("Remover") { dialog, _ ->
val codigo = input.text.toString()
if (codigo.isNotEmpty()) {
removeCodigo(codigo)
} else {
Toast.makeText(this, "Por favor, insira um código válido", Toast.LENGTH_SHORT).show()
}
dialog.dismiss()
}
builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

builder.show()
}

private fun removeCodigo(codigo: String) {
val file = File(filesDir, "codigos.txt")
if (file.exists()) {
val codigos = file.readLines().toMutableList()
val codigoToRemove = codigos.find { it.startsWith("$codigo:") }
if (codigoToRemove != null) {
AlertDialog.Builder(this)
.setTitle("Remover Código")
.setMessage("Deseja remover o código $codigoToRemove?")
.setPositiveButton("Sim") { dialog, _ ->
codigos.remove(codigoToRemove)
file.writeText(codigos.joinToString("\n"))
loadCodigosFromFile() // Atualiza o spinner
Toast.makeText(this, "Código removido com sucesso", Toast.LENGTH_SHORT).show()
dialog.dismiss()
}
.setNegativeButton("Não") { dialog, _ -> dialog.dismiss() }
.show()
} else {
Toast.makeText(this, "Código não encontrado", Toast.LENGTH_SHORT).show()
}
} else {
Toast.makeText(this, "Arquivo codigos.txt não encontrado!", Toast.LENGTH_SHORT).show()
}
}

override fun onDestroy() {
super.onDestroy()
clearHistoryFile()
}

private fun clearHistoryFile() {
val file = File(filesDir, "historico.txt")
if (file.exists()) {
file.delete()
}
}

private fun createDefaultCodigosFile() {
val file = File(filesDir, "codigos.txt")
if (!file.exists()) {
val defaultCodigos = """
001: Material 1 (Tipo: MP)
002: Material 2 (Tipo: PA, Quantidade na Ráfia: 10, Valor Unitário: 1.5)
003: Material 3 (Tipo: PI, Quantidade na Ráfia: 8, Valor Unitário: 2.0)
""".trimIndent()
file.writeText(defaultCodigos)
}
}

private fun loadCodigosFromFile() {
val file = File(filesDir, "codigos.txt")
if (file.exists()) {
val codigos = file.readLines().map { it.split(": ") }
val adapter = ArrayAdapter(
this,
android.R.layout.simple_spinner_item,
codigos.map { "${it[0]} - ${it[1].split(" ")[0]}" }
)
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
spinner.adapter = adapter

spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
val selectedCode = codigos[position][0]
val selectedDescription = codigos[position][1]
if (codigoMaterial != selectedCode) {
textArea.append("Código do material selecionado: $selectedCode\n")
codigoMaterial = selectedCode
tipoMaterial = if (selectedDescription.contains("Tipo: MP")) "MP"
else if (selectedDescription.contains("Tipo: PA")) "PA"
else "PI"
quantidadeRafia = if (tipoMaterial != "MP") selectedDescription.substringAfter("Quantidade na Ráfia: ").substringBefore(",").toDoubleOrNull() ?: 0.0 else 0.0
valorUnitario = if (tipoMaterial != "MP") selectedDescription.substringAfter("Valor Unitário: ").toDoubleOrNull() ?: 0.0 else 0.0
}
}

override fun onNothingSelected(parent: AdapterView<*>) {}
}
} else {
Toast.makeText(this, "Arquivo codigos.txt não encontrado!", Toast.LENGTH_SHORT).show()
}
}

private fun showInputDialog(tipo: String) {
val builder = AlertDialog.Builder(this)
builder.setTitle("Adicionar Peso")

val input = EditText(this)
if (tipoMaterial == "MP") {
input.hint = "Digite o peso em kg"
} else {
input.hint = "Digite o número de unidades"
}
val scrollView = ScrollView(this)
scrollView.addView(input)
builder.setView(scrollView)

builder.setPositiveButton("OK") { dialog, _ ->
val valor = input.text.toString().toDoubleOrNull()
if (valor != null && valor > 0) {
if (tipoMaterial == "MP") {
addPeso(tipo, valor)
} else {
addPesoParaUnidades(tipo, valor.toInt())
}
} else {
Toast.makeText(this, "Por favor, insira um valor válido", Toast.LENGTH_SHORT).show()
}
dialog.dismiss()
}
builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

builder.show()
}

private fun addPeso(tipo: String, valor: Double) {
when (tipo.lowercase()) {
"n" -> {
pesos.add(valor)
totalNatural += valor
resultados[codigoMaterial] = resultados.getOrDefault(codigoMaterial, 0.0) + valor
textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg natural (sem pigmento)\n")
}
	"b" -> {
	pesos.add(valor)
	totalPigmentoBranco += valor * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["branco"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("branco", 0.0) + valor * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg com 2% de pigmento branco\n")
	}
	"p" -> {
	pesos.add(valor)
    totalPigmentoPreto += valor * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["preto"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("preto", 0.0) + valor * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg com 2% de pigmento preto\n")
	}
	"y" -> {
	pesos.add(valor)
	totalPigmentoAmarelo += valor * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["amarelo"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("amarelo", 0.0) + valor * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg com 2% de pigmento amarelo\n")
	}
	"g" -> {
	pesos.add(valor)
	totalPigmentoVerde += valor * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["verde"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("verde", 0.0) + valor * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg com 2% de pigmento verde\n")
	}
	"r" -> {
	pesos.add(valor)
	totalPigmentoVermelho += valor * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["vermelho"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("vermelho", 0.0) + valor * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valor)} kg com 2% de pigmento vermelho\n")
	}
	else -> textArea.append("Tipo de pigmento inválido.\n")
	}
	}
		
	private fun addPesoParaUnidades(tipo: String, unidades: Int) {
	val valorCalculado = unidades * quantidadeRafia * valorUnitario // Multiplica unidades pela quantidade na ráfia e valor unitário
	
	when (tipo.lowercase()) {
	"n" -> {
	pesos.add(valorCalculado)
	totalNatural += valorCalculado
	resultados[codigoMaterial] = resultados.getOrDefault(codigoMaterial, 0.0) + valorCalculado
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg natural (sem pigmento)\n")
	}
	"b" -> {
	pesos.add(valorCalculado)
	totalPigmentoBranco += valorCalculado * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["branco"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("branco", 0.0) + valorCalculado * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg com 2% de pigmento branco\n")
	}
	"p" -> {
	pesos.add(valorCalculado)
	totalPigmentoPreto += valorCalculado * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["preto"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("preto", 0.0) + valorCalculado * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg com 2% de pigmento preto\n")
	}
	"y" -> {
	pesos.add(valorCalculado)
	totalPigmentoAmarelo += valorCalculado * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["amarelo"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("amarelo", 0.0) + valorCalculado * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg com 2% de pigmento amarelo\n")
	}
	"g" -> {
	pesos.add(valorCalculado)
	totalPigmentoVerde += valorCalculado * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["verde"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("verde", 0.0) + valorCalculado * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg com 2% de pigmento verde\n")
	}
	"r" -> {
	pesos.add(valorCalculado)
	totalPigmentoVermelho += valorCalculado * 0.02
	pigmentos.getOrPut(codigoMaterial) { mutableMapOf() }["vermelho"] = pigmentos.getOrDefault(codigoMaterial, mutableMapOf()).getOrDefault("vermelho", 0.0) + valorCalculado * 0.02
	textArea.append("Valor adicionado: ${String.format("%.1f", valorCalculado)} kg com 2% de pigmento vermelho\n")
	}
	else -> textArea.append("Tipo de pigmento inválido.\n")
	}
	}
	
	private fun calculateTotal() {
	val soma = pesos.sum()
	textArea.append("O total do Natural é: ${String.format("%.1f", soma - totalPigmentoBranco - totalPigmentoPreto - totalPigmentoAmarelo - totalPigmentoVerde - totalPigmentoVermelho)} kg\n")
	textArea.append("Total de pigmento branco: ${String.format("%.1f", totalPigmentoBranco)} kg\n")
	textArea.append("Total de pigmento preto: ${String.format("%.1f", totalPigmentoPreto)} kg\n")
	textArea.append("Total de pigmento amarelo: ${String.format("%.1f", totalPigmentoAmarelo)} kg\n")
	textArea.append("Total de pigmento verde: ${String.format("%.1f", totalPigmentoVerde)} kg\n")
	textArea.append("Total de pigmento vermelho: ${String.format("%.1f", totalPigmentoVermelho)} kg\n")
	textArea.append("Total natural (sem pigmento): ${String.format("%.1f", totalNatural)} kg\n")
	textArea.append("--------------------------\n")  // Linha de separação
	}
	
	private fun clearCalculations() {
	// Verifica se há um código de material pré-selecionado antes de salvar o histórico
	if (codigoMaterial.isNotEmpty() && pesos.isNotEmpty()) {
	saveCurrentResultsToHistory()
	}
	
	// Limpa todas as listas e variáveis
	pesos.clear()
	totalPigmentoBranco = 0.0
	totalPigmentoPreto = 0.0
	totalPigmentoAmarelo = 0.0
	totalPigmentoVerde = 0.0
	totalPigmentoVermelho = 0.0
	totalNatural = 0.0
	unidades = 0
	textArea.text = "Cálculos limpos. Pronto para iniciar um novo cálculo.\n"
	}
	
	// Função para salvar os resultados atuais no histórico
	private fun saveCurrentResultsToHistory() {
	if (pesos.isEmpty()) return // Não adiciona ao histórico se não houver pesos adicionados
	
	val historyEntry = buildString {
	append("Código do material: $codigoMaterial\n")
	append("Total de Natural: ${String.format("%.1f", pesos.sum() - totalPigmentoBranco - totalPigmentoPreto - totalPigmentoAmarelo - totalPigmentoVerde - totalPigmentoVermelho)} kg\n")
	append("Total de pigmento branco: ${String.format("%.1f", totalPigmentoBranco)} kg\n")
	append("Total de pigmento preto: ${String.format("%.1f", totalPigmentoPreto)} kg\n")
	append("Total de pigmento amarelo: ${String.format("%.1f", totalPigmentoAmarelo)} kg\n")
	append("Total de pigmento verde: ${String.format("%.1f", totalPigmentoVerde)} kg\n")
	append("Total de pigmento vermelho: ${String.format("%.1f", totalPigmentoVermelho)} kg\n")
	append("Total natural (sem pigmento): ${String.format("%.1f", totalNatural)} kg\n")
	append("--------------------------\n")
	}
	historico.add(historyEntry)
	
	// Salva o histórico em um arquivo de texto
	val file = File(filesDir, "historico.txt")
	file.appendText(historyEntry)
	}
	
	private fun showHistoryDialog() {
	val builder = AlertDialog.Builder(this)
	builder.setTitle("Histórico de Materiais")
	
	val file = File(filesDir, "historico.txt")
	val historyText = if (file.exists()) file.readText() else "Nenhum histórico encontrado."
	
	val scrollView = ScrollView(this)
	val textView = TextView(this)
	textView.text = historyText
	scrollView.addView(textView)
	builder.setView(scrollView)
	
	builder.setPositiveButton("OK") { dialog, _ ->
	dialog.dismiss()
	}
	
	builder.show()
	}
	}