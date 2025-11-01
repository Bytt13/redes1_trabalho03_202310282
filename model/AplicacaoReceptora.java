/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 22/08/2025
* Ultima alteracao.: 31/10/2025
* Nome.............: AplicacaoReceptora
* Funcao...........: Mostra a mensagem depois de todo o processo de transferencia
*************************************************************** */

package model;

import controller.TelaPrincipalController;

public class AplicacaoReceptora {
/**************************************************************
* Metodo: AplicacaoReceptora
* Funcao: Constroi a aplicacao receptora e exibe a mensagem final na GUI
* @param mensagem | mensagem recebida e decodificada
* @return void 
 * ********************************************************* */
  public AplicacaoReceptora(String mensagem) {
    TelaPrincipalController controller = TelaPrincipalController.getController(); // pega o controller que vamos usar para mostrar a mensagem
    javafx.application.Platform.runLater(() -> {
      // Pega o texto que ja estava la
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