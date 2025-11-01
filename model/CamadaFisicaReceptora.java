/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 21/08/2025
* Ultima alteracao.: 31/10/2025
* Nome.............: CamadaFisicaReceptora
* Funcao...........: Transfere a mensagem decodificada para camada aplicacao receptora
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;
//imports que precisamos

public class CamadaFisicaReceptora {
  public CamadaFisicaTransmissora transmissor;
/**************************************************************
* Metodo: CamadaFisicaReceptora
* Funcao: decodifica os bits e passa eles para camada seguinte
* @param quadro | bits recebidos
* @param transmissor | objeto da camada fisica transmissora
* @return void 
* ********************************************************* */
  public CamadaFisicaReceptora(int[] quadro, CamadaFisicaTransmissora transmissor) {
    this.transmissor = transmissor;
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares(); // Cria um objeto para usarmos as funcoes auxiliares
    TelaPrincipalController controller = TelaPrincipalController.getController(); // pega o controller para podermos usar

    String texto = controller.getCodificacao(); // pega o codigo da codificacao escolhida
    int tipoDeCodificacao = auxiliar.numberCodification(texto); // pega a codificacao escolhida e transforma em int
    int[] fluxoBrutoDeBits; // Cria o fluxo de bits que vamos passar adiante
    
    // Switch para escolher qual decodificacao usar no quadro ja desenquadrado
    switch(tipoDeCodificacao) {
      case 0:
        fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoBinaria(quadro);
        break;
      case 1:
        fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoManchester(quadro);
        break;
      case 2: 
        fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoManchesterDiferencial(quadro);
        break;
      default:
        fluxoBrutoDeBits = CamadaFisicaReceptoraDecodificacaoBinaria(quadro);
        break;
    } // fim do switch
    /* *********************************************************
                        DEBUGGER DE CHEGADA
    ********************************************************* */
    FuncoesAuxiliares auxiliarDebug = new FuncoesAuxiliares();
    System.out.println("\n--- DEBUG: CHEGANDO NA CAMADA FISICA (RX) ---");
    // Usamos 'descobrirTotalDeBitsReais' para nao imprimir o padding
    int totalBitsReaisRx = auxiliarDebug.descobrirTotalDeBitsReais(fluxoBrutoDeBits);
    System.out.println(auxiliarDebug.arrayDeBitsParaString(fluxoBrutoDeBits, totalBitsReaisRx));
    System.out.println("---------------------------------------------\n");
    /* *********************************************************
                          FIM DO DEBUGGER
    ********************************************************* */
    // Chama a proxima camada
    new CamadaEnlaceDadosReceptora(fluxoBrutoDeBits, transmissor);
  } // Fim do metodo


  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDecodificacaoBinaria
  * Funcao: envia a mensagem (em bits) decodificada em binario para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return int[] | a mensagem eh igual aos bits em binario 
  * ********************************************************* */
  public static int[] CamadaFisicaReceptoraDecodificacaoBinaria(int[] quadro) {
    return quadro; // Em binario, os bits ja estao na forma final
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDecodificacaoManchester
  * Funcao: envia a mensagem (em bits) decodificada em manchester para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return int[] | a mensagem decodificada em manchester
  * ********************************************************* */
  public static int[] CamadaFisicaReceptoraDecodificacaoManchester(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    //if para verificar se foi violacao da camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      // Passa o 'tipoDeCodificacao' (1 = Manchester)
      return CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(quadro, 1);
    } // fim do if
    
    int bitsCodificados = quadro.length * 32;
    int bitsOriginais = bitsCodificados / 2; // O resultado tera metade dos bits
    
    int tamanhoArrayDecodificado = (bitsOriginais + 31) / 32;
    int[] decodificado = new int[tamanhoArrayDecodificado];

    //for para percorrer os bits e realizar a decodificacao em manchester
    for (int i = 0; i < bitsOriginais; i++) {
        //Define a posicao do par de bits que vamos ler no quadro codificado.
        //Nos so precisamos do primeiro bit do par, que esta na posicao i*2.
        int indiceCodificado = i * 2;
        int indiceIntCodificado = indiceCodificado / 32;
        int indiceBitCodificado = 31 - (indiceCodificado % 32);

        //Le o primeiro bit do par, que eh sempre o bit original na codificacao Manchester.
        int bitOriginal = (quadro[indiceIntCodificado] >> indiceBitCodificado) & 1;

        //Define a posicao onde o bit original sera gravado no array de destino.
        int pos = i / 32;
        int intPos = 31 - (i % 32);

        //Grava o bit original no array decodificado (se o bit for 1).
        //if para verificar o valor do bit
        if (bitOriginal == 1) {
            decodificado[pos] |= (1 << intPos);
        } // fim do if
    } // fim do for
    return decodificado; // retorno da funcao
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDecodificacaoManchesterDiferencial
  * Funcao: envia a mensagem (em bits) deodificada em manchester diferencial para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return int[] | a mensagem decodificada em mancheser diferencial
  * ********************************************************* */
  public static int[] CamadaFisicaReceptoraDecodificacaoManchesterDiferencial(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    //if para verificar se foi violacao da camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      // Passa o 'tipoDeCodificacao' (2 = Manchester Diferencial)
      return CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(quadro, 2);
    } // fim do if

    int bitsCodificados = quadro.length * 32;
    int bitsOriginais = bitsCodificados / 2; // O resultado tera metade dos bits

    int tamanhoArrayDecodificado = (bitsOriginais + 31) / 32;
    int[] decodificado = new int[tamanhoArrayDecodificado];

    // O ultimo nivel de sinal que vimos. Comecamos com 1 por escolha (deve ser o mesmo do transmissor).
    int ultimoNivel = 1;

    //for para percorrer os bits e realizar a decodificacao de manchester diferencial
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o par de bits codificados
        int indiceGeralBit1 = i * 2;
        int indiceInt1 = indiceGeralBit1 / 32;
        int indiceBitNoInt1 = 31 - (indiceGeralBit1 % 32);
        int primeiroNivelDoPar = (quadro[indiceInt1] >> indiceBitNoInt1) & 1;

        int indiceGeralBit2 = (i * 2) + 1;
        int indiceInt2 = indiceGeralBit2 / 32;
        int indiceBitNoInt2 = 31 - (indiceGeralBit2 % 32);
        int segundoNivelDoPar = (quadro[indiceInt2] >> indiceBitNoInt2) & 1;

        int bitOriginal;
        // Compara o final do bit anterior (ultimoNivel) com o inicio do atual (primeiroNivelDoPar)
        //if para verificar o nivel de sinal
        if (ultimoNivel == primeiroNivelDoPar) {
            // Se nao houve transicao, o bit eh 1.
            bitOriginal = 1;
        } else {
            // Se houve transicao, o bit eh 0.
            bitOriginal = 0;
        } // fim do if-else
        
        // Define a posicao onde o bit original sera gravado no array de destino.
        int pos = i / 32;
        int intPos = 31 - (i % 32);
        //if para verificar o valor do bit
        if (bitOriginal == 1) {
            decodificado[pos] |= (1 << intPos);
        } // fim do if

        // O "ultimo nivel" para a proxima iteracao sera o segundo nivel do par atual.
        ultimoNivel = segundoNivelDoPar;
    } // fim do for

    return decodificado; // retorno da funcao
    
  } // Fim do metodo
  
  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDesenquadramentoViolacaoFisica
  * Funcao: Remove as flags de inicio e fim (1100) do enquadramento de violacao da camada fisica.
  * @param quadro | quadro de bits com as flags
  * @param tipoDeDecodificacao | 1 para Manchester, 2 para Diferencial
  * @return int[] | novo quadro sem as flags
  * ********************************************************* */
  public static int[] CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(int[] quadro, int tipoDeDecodificacao) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    final int VIOLACAO = 0b1111;
    final int TAMANHO_VIOLACAO_BITS = 4;

    // Usar 'descobrirTotalDeBitsReais'
    // Precisamos saber o tamanho real do *sinal* recebido,
    // para nao tentar ler o "padding" do array.
    int totalBitsSinal = auxiliar.descobrirTotalDeBitsReais(quadro);
    
    if (totalBitsSinal == 0)
      return new int[0];

    int[] quadroDecodificado = new int[quadro.length]; // buffer temporario
    int bitEscritaGlobal = 0;
    boolean quadroIniciado = false;
    int nivelAnterior = 1; // para Manchester Diferencial

    int i = 0; 
    // while para melhor controle do indice manualmente
    while (i <= totalBitsSinal - 2) { // garante que pelo menos um par manchester existe

      // if paraverificar se ha uma VIOLACAO (1100) na posicao atual
      if (i <= totalBitsSinal - TAMANHO_VIOLACAO_BITS) {
        int possivelViolacao = auxiliar.lerBits(quadro, i, TAMANHO_VIOLACAO_BITS);
        // if para verificar a violacao
        if (possivelViolacao == VIOLACAO) {
          quadroIniciado = true; // marca que o processamento de dados pode comecar
          i += TAMANHO_VIOLACAO_BITS; // pula os 4 bits da violacao
          
          // Resetar o nivel do Manchester Diferencial
          // Sincroniza o receptor com o transmissor toda vez que uma
          // flag eh detectada.
          nivelAnterior = 1; 
          
          continue; // volta ao inicio do loo
        } // fim do if
      } // fim do if

      // if para verificar se o quadro ainda nao foi iniciado
      if (!quadroIniciado) {
        i++; // vai avancando
        continue;
      } // fim do if

      // decodifica os dados
      int bit1 = auxiliar.lerBits(quadro, i, 1);
      int bit2 = auxiliar.lerBits(quadro, i + 1, 1);

      int bitOriginal = 0;
      if (tipoDeDecodificacao == 1) { // Manchester
        if (bit1 == 1 && bit2 == 0) { // 10->1
          bitOriginal = 1;
        } else {
          bitOriginal = 0; // 01->0
        }
      } else { // Manchester Diferencial
        if (bit1 == nivelAnterior) { // sem transicao -> 1
          bitOriginal = 1;
        } else {
          bitOriginal = 0; // com transicao -> 0
        }
        nivelAnterior = bit2; // atualiza o nivel para a proxima comparacao
      }

      auxiliar.escreverBits(quadroDecodificado, bitEscritaGlobal++, bitOriginal, 1);

      i += 2; // avanca para o proximo par de bits do sinal

    } // fim do while

    // ajusta o array final para o tamanho exato dos bits decodificados
    int tamanhoFinalArray = (bitEscritaGlobal + 31) / 32;
    int[] resultadoFinal = new int[tamanhoFinalArray];
    for (int j = 0; j < bitEscritaGlobal; j++) {
      int bit = auxiliar.lerBits(quadroDecodificado, j, 1);
      auxiliar.escreverBits(resultadoFinal, j, bit, 1);
    }
    return resultadoFinal;
  }// fim do metodo
} // fim da classe