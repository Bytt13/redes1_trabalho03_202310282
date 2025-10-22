/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/08/2025
* Ultima alteracao.: 21/10/2025 (Refatoracao Concorrente)
* Nome.............: MeioDeComunicacao
* Funcao...........: Simula a transferencia de UM subquadro, aplica
* erro por subquadro, e gerencia a rota de ACK.
*************************************************************** */
package model;

import java.util.Random;
import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class MeioDeComunicacao {

  // Referencias para os dois lados da comunicacao
  private CamadaFisicaTransmissora transmissor;
  private CamadaFisicaReceptora receptor;

/**************************************************************
* Metodo: MeioDeComunicacao (Construtor Refatorado)
* Funcao: Cria a instancia do receptor e o linka a este meio.
* @param void
* @return void 
* ********************************************************* */
  public MeioDeComunicacao() {
    // O Meio agora eh responsavel por criar o lado receptor
    this.receptor = new CamadaFisicaReceptora();
    // Linka o receptor DE VOLTA para este meio (para ACKs)
    this.receptor.setMeio(this);
  }

  /**************************************************************
  * Metodo: setTransmissor
  * Funcao: Linka o lado transmissor (que iniciou a chamada)
  * a este meio (para rota de ACK).
  * @param t | O transmissor
  * @return void 
  * ********************************************************* */
  public void setTransmissor(CamadaFisicaTransmissora t) {
    this.transmissor = t;
  }

  /**************************************************************
  * Metodo: transferir (NOVO - Antigo Construtor)
  * Funcao: transfere UM subquadro, aplicando a logica de erros
  * APENAS a este subquadro.
  * @param fluxoBrutoDeBits | O subquadro codificado
  * @return void 
  * ********************************************************* */
  public void transferir(int[] fluxoBrutoDeBits) {
    TelaPrincipalController controller  = TelaPrincipalController.getController();
    Random random = new Random();
    double taxaDeErro = controller.getTaxaDeErro();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBitsPontoA.length];
    
    int totalDeBitsReais = auxiliar.descobrirTotalDeBitsReais(fluxoBrutoDeBitsPontoA);
    
    int posicaoDoErroNesteQuadro = -1; // -1 significa que nao ha erro

    // Requisito 4: Sorteia se O SUBQUADRO ATUAL tera um erro
    if (random.nextDouble() < taxaDeErro && totalDeBitsReais > 0) {
        // Sorteia a POSICAO do erro dentro do subquadro
        posicaoDoErroNesteQuadro = random.nextInt(totalDeBitsReais);
        System.out.println("ERRO INJETADO no bit:");
        System.out.println(posicaoDoErroNesteQuadro);
    }
    
    // Loop principal que simula a transferencia bit a bit DO SUBQUADRO
    for (int i = 0; i < totalDeBitsReais; i++) {
        
        // Transfere o bit original de A para B
        int bitOriginal = auxiliar.lerBits(fluxoBrutoDeBitsPontoA, i, 1);
        auxiliar.escreverBits(fluxoBrutoDeBitsPontoB, i, bitOriginal, 1);

        // Agora, checa se esse bit (na posicao 'i') eh o que deve ser corrompido.
        if (i == posicaoDoErroNesteQuadro) {
            // Se for, aplicamos a inversao diretamente no array de destino (PontoB).
            int indiceDoInt = i / 32;
            int posNoInt = 31 - (i % 32);
            fluxoBrutoDeBitsPontoB[indiceDoInt] ^= (1 << posNoInt);
        }

    } // Fim do for de transferencia bit a bit
    
    // Entrega o subquadro (corrompido ou nao) ao receptor
    receptor.receberSubquadro(fluxoBrutoDeBitsPontoB);
  } // Fim do metodo transferir

  /**************************************************************
  * Metodo: enviarAck (NOVO)
  * Funcao: Rota de retorno para o ACK. Chamado pelo receptor.
  * @param ackQuadro | O quadro de ACK
  * @return void 
  * ********************************************************* */
  public void enviarAck(int[] ackQuadro) {
    // Simula o retorno do ACK (sem erros)
    if (transmissor != null) {
      transmissor.receberAck(ackQuadro);
    }
  } // Fim do metodo enviarAck
} // Fim da classe