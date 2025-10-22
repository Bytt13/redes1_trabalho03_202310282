/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 22/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: CamadaDeAplicacaoReceptora
* Funcao...........: Transfere a mensagem convertida de binario para texto para aplicacao receptora
*************************************************************** */

package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaDeAplicacaoReceptora {
  /**************************************************************
  * Metodo: CamadaDeAplicacaoReceptora
  * Funcao: recebe os bits e passa eles para camada seguinte em forma de texto
  * @param  quadro | bits recebidos (SUBQUADRO desenquadrado)
  * @return void 
  * ********************************************************* */
    public CamadaDeAplicacaoReceptora(int[] quadro) {
      FuncoesAuxiliares auxiliar = new FuncoesAuxiliares(); // cria o objeto para podermos usar as funcoes auxiliares
      TelaPrincipalController controller = TelaPrincipalController.getController();
      // String mensagemOriginal = controller.getMensagemOriginal(); // REMOVIDO
      
      int totalBits = auxiliar.descobrirTotalDeBitsReais(quadro);
      if (totalBits == 0 && quadro.length > 0) totalBits = 32; // Assume 32 bits se o quadro for 0x00
      
      // Usa a funcao para obter a string binaria
      final String bitsDecodificados = auxiliar.arrayDeBitsParaString(quadro, totalBits);
      
      // Atualiza a GUI na thread do JavaFX
    javafx.application.Platform.runLater(() -> {
    // --- INICIO DA CORRECAO ---
    // Pega o texto atual e anexa o novo, usando o getter que criamos
    String textoAtual = controller.getTextAreaDecodificada();
    StringBuilder sb = new StringBuilder(textoAtual);
    if (!textoAtual.isEmpty()) {
        sb.append("\n"); // Adiciona uma nova linha para separar os quadros
    }
    sb.append(bitsDecodificados);
    controller.setTextAreaDecodificada(sb.toString()); // Envia o texto acumulado
    // --- FIM DA CORRECAO ---
});

      // Transformando os binarios em texto
      // Agora usamos quadro.length * 4 (bytes) como maximo
      String mensagem = auxiliar.binaryArrayToString(quadro, quadro.length * 4);
      
      // chama a proxima camada
      new AplicacaoReceptora(mensagem);
    } // fim do metodo
} // fim da classe