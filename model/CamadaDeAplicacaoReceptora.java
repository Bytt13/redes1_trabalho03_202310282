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
      int totalBits = quadro.length * 32;
      if(totalBits == 0 && quadro.length > 0) totalBits = 32;
      final String bitsDecodificados = auxiliar.arrayDeBitsParaString(quadro, totalBits);
      javafx.application.Platform.runLater(() -> {
        String textoAtual = controller.getTextAreaDecodificada();
        StringBuilder sb = new StringBuilder(textoAtual);
        if (!textoAtual.isEmpty()) {
            sb.append("\n"); // Adiciona uma nova linha para separar os quadros
        }
        sb.append(bitsDecodificados);
        controller.setTextAreaDecodificada(sb.toString()); 
      });

      // Transformando os binarios em texto
      String mensagem = auxiliar.binaryArrayToString(quadro, quadro.length * 4);
      // chama a proxima camada
      new AplicacaoReceptora(mensagem);
    } // fim do metodo
} // fim da classe
