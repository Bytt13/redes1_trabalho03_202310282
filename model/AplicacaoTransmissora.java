/***************************************************************** * Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 19/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: AplicacaoTransmissora
* Funcao...........: Envia a mensagem captada pelo controller para a proxima camada da aplicacao
*************************************************************** */

package model;

import controller.TelaPrincipalController;

public class AplicacaoTransmissora implements Runnable {

/**************************************************************
* Metodo: AplicacaoTransmissora (Construtor)
* Funcao: Vazio, pois a logica agora esta no metodo run().
* @param void
* @return void 
 * ********************************************************* */
  public AplicacaoTransmissora() {
    // A logica foi movida para o metodo run()
  } // Fim do metodo

  /**************************************************************
  * Metodo: run
  * Funcao: Pega a mensagem e inicia a pilha de transmissao.
  * Executado pela Thread iniciada no TelaPrincipalController.
  * @param void
  * @return void 
  * ********************************************************* */
  @Override
  public void run() {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    String mensagem = controller.getMensagemOriginal(); // pega a mensagem que foi escrita na tela inicial
    // Chama a proxima camada
    new CamadaDeAplicacaoTransmissora(mensagem);
  } // Fim do metodo
} // Fim da classe