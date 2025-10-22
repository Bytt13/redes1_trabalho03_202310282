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
  * @param  quadro | bits recebidos
  * @return void 
  * ********************************************************* */
    public CamadaDeAplicacaoReceptora(int[] quadro) {
      FuncoesAuxiliares auxiliar = new FuncoesAuxiliares(); // cria o objeto para podermos usar as funcoes auxiliares
      TelaPrincipalController controller = TelaPrincipalController.getController();
      String mensagemOriginal = controller.getMensagemOriginal();
      int totalBits = quadro.length * 32;
      // Calcula o numero de bits exatos da mensagem original
      // Usa a funcao para obter a string binaria
      String bitsDecodificados = auxiliar.arrayDeBitsParaString(quadro, totalBits);
      controller.setTextAreaDecodificada(bitsDecodificados);

      // Transformando os binarios em texto
      String mensagem = auxiliar.binaryArrayToString(quadro, mensagemOriginal.length());
      // chama a proxima camada
      new AplicacaoReceptora(mensagem);
    } // fim do metodo
} // fim da classe
