/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 22/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: AplicacaoReceptora
* Funcao...........: MOstra a mensagem depois de todo o processo de transferencia
*************************************************************** */

package model;

import controller.TelaPrincipalController;

public class AplicacaoReceptora {
/**************************************************************
* Metodo: AplicacaoTransmissora
* Funcao: envia a mensagem em forma de string para a proxima camada
* @param mensagem | mensagem que o usuario digitou, ja transformada
* @return void 
 * ********************************************************* */
  public AplicacaoReceptora(String mensagem) {
    TelaPrincipalController controller = TelaPrincipalController.getController(); // pega o controller que vamos usar para mostrar a mensagem
    javafx.application.Platform.runLater(() -> {
      // Pega o texto que ja estava la (usando o novo getter do Passo 1)
      String textoAtual = controller.getTextAreaMensagemFinal();
      
      // Concatena a nova parte da mensagem
      StringBuilder sb = new StringBuilder(textoAtual);
      
      // Anexa a nova parte da mensagem
      sb.append(mensagem);
      
      // Define o texto completo de volta na GUI
      controller.setTextAreaMensagemFinal(sb.toString());
    });
  }// Fim do metodo
} // Fim da classe
