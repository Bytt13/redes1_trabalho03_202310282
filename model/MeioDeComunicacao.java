/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 20/08/2025
* Ultima alteracao.: 31/10/2025
* Nome.............: MeioDeComunicacao
* Funcao...........: Transfere a mensagem codificada, aplicando chance de erro por quadro de enquadramento.
*************************************************************** */
package model;

import java.util.Random;
import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class MeioDeComunicacao {
private CamadaFisicaTransmissora transmissor;
/**************************************************************
* Metodo: MeioDeComunicacao
* Funcao: transfere a mensagem em forma de bits, aplicando a logica de erros por quadro.
* @param fluxoBrutoDeBits | fluxo de bits recebido
* @param transmissor | objeto da camada fisica transmissora
* @return void 
* ********************************************************* */
  public MeioDeComunicacao(int[] fluxoBrutoDeBits, CamadaFisicaTransmissora transmissor) {
    TelaPrincipalController controller  = TelaPrincipalController.getController();
    this.transmissor = transmissor;
    Random random = new Random();
    double taxaDeErro = controller.getTaxaDeErro();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBitsPontoA.length];
    
    int totalDeBitsReais = fluxoBrutoDeBitsPontoA.length * 32;
    
    String enquadramento = controller.getEnquadramento();

    int tamanhoLogicoDoQuadroEmBits;
    switch (enquadramento) {
      case "Contagem de Caracteres":
      case "Insercao de bytes":
      case "Insercao de bits":
        tamanhoLogicoDoQuadroEmBits = 40; // 5 bytes
        break;
      case "Violacao da Camada Fisica":
      default:
        tamanhoLogicoDoQuadroEmBits = totalDeBitsReais;
        break;
    }

    int tamanhoFisicoDoQuadroEmBits = tamanhoLogicoDoQuadroEmBits;
    
    // Evita divisao por zero se o quadro for vazio
    if (tamanhoFisicoDoQuadroEmBits <= 0) {
      new CamadaFisicaReceptora(fluxoBrutoDeBitsPontoB, transmissor);
      return;
    }
    
    int posicaoDoErroNesteQuadro = -1; // -1 significa que nao ha erro agendado para o quadro atual

    // Loop principal que simula a transferencia bit a bit 
    for (int i = 0; i < totalDeBitsReais; i++) {
        // Verifica se estamos no inicio de um novo quadro para sortear um erro
        if (i % tamanhoFisicoDoQuadroEmBits == 0) {
            posicaoDoErroNesteQuadro = -1; // Reseta o erro do quadro anterior
            // Sorteia se o quadro ATUAL tera um erro
            if (random.nextDouble() < taxaDeErro) {
                // Define o tamanho real deste quadro (pode ser menor no final da transmissao)
                int fimDoQuadro = Math.min(i + tamanhoFisicoDoQuadroEmBits, totalDeBitsReais);
                int tamanhoRealDoQuadroAtual = fimDoQuadro - i;

                // Sorteia a POSICAO do erro dentro do quadro e calcula a posicao global
                int bitAleatorioNoQuadro = random.nextInt(tamanhoRealDoQuadroAtual);
                posicaoDoErroNesteQuadro = i + bitAleatorioNoQuadro;
            }
        }
        
        // Transfere o bit original de A para B, incondicionalmente.
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
    
    new CamadaFisicaReceptora(fluxoBrutoDeBitsPontoB, transmissor);
  } // Fim do metodo

/**************************************************************
* Metodo: transferirAck
* Funcao: transfere um quadro de ACK/NACK, aplicando a mesma logica de erros.
* @param fluxoBrutoDeBits | fluxo de bits (ACK/NACK)
* @param transmissor | objeto da camada fisica transmissora para retorno
* @return void 
* ********************************************************* */
  public static void transferirAck(int[] fluxoBrutoDeBits, CamadaFisicaTransmissora transmissor) {
    TelaPrincipalController controller  = TelaPrincipalController.getController();
    Random random = new Random();
    double taxaDeErro = controller.getTaxaDeErro();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    int[] fluxoBrutoDeBitsPontoA = fluxoBrutoDeBits;
    int[] fluxoBrutoDeBitsPontoB = new int[fluxoBrutoDeBitsPontoA.length];
    
    int totalDeBitsReais = fluxoBrutoDeBitsPontoA.length * 32;
    
    String enquadramento = controller.getEnquadramento();

    int tamanhoLogicoDoQuadroEmBits;
    switch (enquadramento) {
      case "Contagem de Caracteres":
      case "Insercao de bytes":
      case "Insercao de bits":
        tamanhoLogicoDoQuadroEmBits = 40; // 5 bytes
        break;
      case "Violacao da Camada Fisica":
      default:
        tamanhoLogicoDoQuadroEmBits = totalDeBitsReais;
        break;
    }

    int tamanhoFisicoDoQuadroEmBits = tamanhoLogicoDoQuadroEmBits;
    
    // Evita divisao por zero se o quadro for vazio
    if (tamanhoFisicoDoQuadroEmBits <= 0) {
      new CamadaFisicaReceptora(fluxoBrutoDeBitsPontoB, transmissor);
      return;
    }
    
    int posicaoDoErroNesteQuadro = -1; // -1 significa que nao ha erro agendado para o quadro atual

    // Loop principal que simula a transferencia bit a bit 
    for (int i = 0; i < totalDeBitsReais; i++) {
        // Verifica se estamos no inicio de um novo quadro para sortear um erro
        if (i % tamanhoFisicoDoQuadroEmBits == 0) {
            posicaoDoErroNesteQuadro = -1; // Reseta o erro do quadro anterior
            // Sorteia se o quadro ATUAL tera um erro
            if (random.nextDouble() < taxaDeErro) {
                // Define o tamanho real deste quadro (pode ser menor no final da transmissao)
                int fimDoQuadro = Math.min(i + tamanhoFisicoDoQuadroEmBits, totalDeBitsReais);
                int tamanhoRealDoQuadroAtual = fimDoQuadro - i;

                // Sorteia a POSICAO do erro dentro do quadro e calcula a posicao global
                int bitAleatorioNoQuadro = random.nextInt(tamanhoRealDoQuadroAtual);
                posicaoDoErroNesteQuadro = i + bitAleatorioNoQuadro;
            }
        }
        
        // Transfere o bit original de A para B, incondicionalmente.
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
    
    transmissor.receberAck(fluxoBrutoDeBitsPontoB);
  } // Fim do metodo
} // Fim da classe