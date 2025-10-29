/***************************************************************** * Autor..............: Lucas de Menezes Chaves
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
    
    // MODIFICADO: Usa appendText para construir a mensagem quadro a quadro
    controller.setTextAreaMensagemFinal(mensagem); 
    
    // if para comparar as mensagens e emitir o alerta
  }// Fim do metodo
} // Fim da classe