/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 18/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: CamadaDeAplicacaoTransmissora
* Funcao...........: Transfere a mensagem em binario para camada seguinte
*************************************************************** */
package model;

import utils.FuncoesAuxiliares;

public class CamadaDeAplicacaoTransmissora {
/**************************************************************
* Metodo: CamadaAplicacaoTransmissora
* Funcao: envia a mensagem em forma de bits para a proxima camada
* @param mensagem | mensagem em forma de texto
* @return void 
* ********************************************************* */
  public CamadaDeAplicacaoTransmissora(String mensagem) {
    int[] quadro; // Cria a variavel que retorna o quadro de bits
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares(); // Cria uma instancia para as funcoes auxiliares
    quadro = auxiliar.stringToBinary(mensagem); // Transforma o texto de string para binario 
    // Chama a proxima camada
    new CamadaEnlaceDadosTransmissora(quadro);
  } // Fim do metodo
} // Fim da classe
