/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 19/08/2025
* Ultima alteracao.: 31/10/2025
* Nome.............: AplicacaoTransmissora
* Funcao...........: Envia a mensagem captada pelo controller para a proxima camada da aplicacao
*************************************************************** */

package model;

import controller.TelaPrincipalController;

public class AplicacaoTransmissora {
/**************************************************************
* Metodo: AplicacaoTransmissora
* Funcao: envia a mensagem em forma de string para a proxima camada
* @param void
* @return void 
 * ********************************************************* */
  public AplicacaoTransmissora() {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    String mensagem = controller.getMensagemOriginal(); // pega a mensagem que foi escrita na tela inicial
    // Chama a proxima camada
    new CamadaDeAplicacaoTransmissora(mensagem);
  } // Fim do metodo
} // Fim da classe