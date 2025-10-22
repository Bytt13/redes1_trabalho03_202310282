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
* @param mensagem | mensagem que o usuario digitou, ja transformada (AGORA UM SUBQUADRO)
* @return void 
 * ********************************************************* */
  public AplicacaoReceptora(String mensagem) {
    // Como varias threads chamam isso, precisamos atualizar a GUI
    // de forma segura (na thread do JavaFX) e acumulativa.
    
    javafx.application.Platform.runLater(() -> {
        TelaPrincipalController controller = TelaPrincipalController.getController(); // pega o controller
        
        // Acumula o texto
        String textoAtual = controller.getTextAreaMensagemFinal();
        StringBuilder sb = new StringBuilder(textoAtual);
        sb.append(mensagem);
        controller.setTextAreaMensagemFinal(sb.toString()); // mostra a mensagem na caixa de texto da GUI
    });
  }// Fim do metodo
} // Fim da classe
