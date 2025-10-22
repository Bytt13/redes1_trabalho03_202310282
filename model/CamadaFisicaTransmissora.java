/***************************************************************** * Autor............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 19/08/2025
* Ultima alteracao.: 21/10/2025 (Refatoracao Concorrente)
* Nome.............: CamadaFisicaTransmissora
* Funcao...........: Codifica e envia UM subquadro. Recebe ACKs.
*************************************************************** */

package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaFisicaTransmissora {
  
  // Referencia ao "onibus" de comunicacao
  private MeioDeComunicacao meio;

  /**************************************************************
  * Metodo: setMeio
  * Funcao: Injeta a dependencia do meio de comunicacao
  * @param m | O meio
  * @return void 
  * ********************************************************* */
  public void setMeio(MeioDeComunicacao m) {
    this.meio = m;
  }

  /**************************************************************
  * Metodo: CamadaFisicaTransmissora (Construtor Refatorado)
  * Funcao: Construtor vazio.
  * ********************************************************* */
  public CamadaFisicaTransmissora() {
    // Vazio. A logica foi movida para 'enviarSubquadro'
  }

  /**************************************************************
  * Metodo: enviarSubquadro (NOVO)
  * Funcao: Pega um subquadro enquadrado, o codifica e o envia
  * para o meio de comunicacao.
  * @param quadro | subquadro enquadrado
  * @return void 
  * ********************************************************* */
  public void enviarSubquadro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeCodificacao = auxiliar.numberCodification(controller.getCodificacao()); // codificacao escolhida
    
    int[] fluxoBrutoDeBits; // Fluxo de bits depois de serem codificados
    
    // switch para codificar corretamente os bits
    switch (tipoDeCodificacao) {
      case 0:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoBinaria(quadro); // Codificacao binaria
        break;
      case 1:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchester(quadro); // codificacao manchester
        break;
      case 2:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(quadro); //codificacao manchester diferencial
        break;
      default:
        fluxoBrutoDeBits = CamadaFisicaTransmissoraCodificacaoBinaria(quadro); //Binario por padrao
        break;
    } // Fim do switch

/* *********************************************************
  ATENCAO, ESSA PARTE EH SOMENTE PARA MOSTRAR NA GUI, NAO TEM VALOR FUNCIONAL
  AGORA PRECISA SER EXECUTADA NA THREAD DA GUI (JavaFX)
********************************************************* */

  int totalDeBitsReais; // Variavel para guardar o nro exato de bits
  String enquadramento = controller.getEnquadramento();

  // Calcula o total de bits reais baseado no enquadramento e codificacao
  if (enquadramento.equals("Contagem de Caracteres")) {
      // Contagem de Caracteres (payload de 4 bytes) -> 1 byte (contagem) + 4 bytes (payload) = 40 bits
      totalDeBitsReais = 40;
      // Manchester/Diff (tipos 1 e 2) dobram os bits
      if (tipoDeCodificacao != 0) {
          totalDeBitsReais *= 2;
      }
  } else {
      // Para os outros metodos (Insercao de Bits, Bytes, Violacao),
      // podemos confiar no ultimo bit '1' setado (pelas flags ou stuffing)
      // para descobrir o tamanho total.
      totalDeBitsReais = auxiliar.descobrirTotalDeBitsReais(fluxoBrutoDeBits);
  }

  // Agora usamos a funcao auxiliar correta para criar a string
  // auxiliar.arrayDeBitsParaString(array_de_ints, total_de_bits_para_mostrar)
  // Isso ira formatar *apenas* os bits relevantes, sem o lixo de zeros no final.
  final String textoCodificado = auxiliar.arrayDeBitsParaString(fluxoBrutoDeBits, totalDeBitsReais);
  final int[] bitsAnimacao = fluxoBrutoDeBits;
  final int bitsParaAnimar = totalDeBitsReais; // A animacao deve usar o mesmo numero de bits

// Atualiza a GUI na thread do JavaFX
javafx.application.Platform.runLater(() -> {
    // Pega o texto atual e anexa o novo, para nao sobrescrever (Correcao da concorrencia)
    String textoAtual = controller.getTextFieldCodificada();
    StringBuilder sbGUI = new StringBuilder(textoAtual);
    if (!textoAtual.isEmpty()) {
        sbGUI.append("\n"); // Adiciona uma nova linha para separar os quadros
    }
    sbGUI.append(textoCodificado);
    controller.setTextAreaCodificada(sbGUI.toString()); // Envia o texto acumulado

    // Inicia a animacao deste subquadro
    auxiliar.animate(controller, bitsAnimacao, bitsParaAnimar);
});
    // Envia o subquadro codificado para o Meio
    meio.transferir(fluxoBrutoDeBits);
  } // Fim do metodo enviarSubquadro
  
  /**************************************************************
  * Metodo: receberAck (NOVO - Requisito 6)
  * Funcao: Metodo de callback chamado pelo Meio quando um ACK
  * chega para este transmissor.
  * @param quadroAck | o quadro de ACK (8 bits)
  * @return void 
  * ********************************************************* */
  public void receberAck(int[] quadroAck) {
    FuncoesAuxiliares aux = new FuncoesAuxiliares();
    int ackBits = aux.lerBits(quadroAck, 0, 8); // Le os 8 bits do ACK

    if (ackBits == 0b10101010) {
        System.out.println("ACK Recebido pela Thread: ");
        System.out.println(Thread.currentThread().getName());
        // Aqui entraria a logica de retransmissao, se houvesse.
        // Por enquanto, apenas confirmamos o recebimento.
    } else {
        System.out.println("Quadro de resposta desconhecido recebido.");
    }
  } // Fim do metodo receberAck

  /*
   * =================================================================
   * OS METODOS ABAIXO (Codificacoes)
   * PERMANECEM OS MESMOS (sao chamados por 'enviarSubquadro')
   * =================================================================
   */
   
  // ... (Metodos CamadaFisicaTransmissoraCodificacao... permanecem inalterados) ...
  
  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoBinaria
  * Funcao: envia a mensagem (em bits) codificada em binario para a proxima camada
  * @param quadro | mensagem recebida (em bits)
  * @return a mensagem eh igual aos bits em binario 
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoBinaria(int[] quadro) {
    return quadro; // Em binario ja eh igual aos bits
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchester
  * Funcao: envia a mensagem (em bits) codificada em manchester para a proxima camada
  * @param quadro | mensagem recebida (em bits) (SUBQUADRO)
  * @return a mensagem codificada em manchester
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchester(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    
    // Descobre o nro de bits REAIS no subquadro (pode ser < 32)
    int bitsOriginais;
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        // Contagem de Caracteres (neste projeto) SEMPRE cria um quadro de 5 bytes (40 bits)
        // (1 byte de contagem + 4 bytes de payload)
        bitsOriginais = 40; 
    } else {
        // A logica antiga (pode ter bugs para outros metodos, mas corrige o atual)
        bitsOriginais = auxiliar.descobrirTotalDeBitsReais(quadro); 
    }
    
    // O array Manchester tera o dobro de bits, calculamos quantos 'ints' sao necessarios
    int numeroDeBitsManchester = bitsOriginais * 2;
    int tamanhoArrayManchester = (numeroDeBitsManchester + 31) / 32; // Formula para arredondar para cima a divisao
    int[] manchester = new int[tamanhoArrayManchester];

    //for para percorrer os bits e realizar a codificacao manchester
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o bit original da posicao i
        int bitOriginal = auxiliar.lerBits(quadro, i, 1);

        // Calcula o par Manchester 
        int bit1 = bitOriginal;
        int bit2 = bitOriginal ^ 1; // operacao XOR para inverter o bit

        // Calcula a posicao dos 2 novos bits no array de destino
        int indiceBit1 = i * 2;
        int indiceBit2 = (i * 2) + 1;

        // Escreve o primeiro bit do par
        auxiliar.escreverBits(manchester, indiceBit1, bit1, 1);
        
        // Escreve o segundo bit do par
        auxiliar.escreverBits(manchester, indiceBit2, bit2, 1);
    }
    return manchester; // retorno da funcao
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoManchesterDiferencial
  * Funcao: envia a mensagem (em bits) codificada em manchester diferencial para a proxima camada
  * @param  quadro | mensagem recebida (em bits) (SUBQUADRO)
  * @return a mensagem codificada em manchester diferencial 
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    int bitsOriginais;
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        // Contagem de Caracteres (neste projeto) SEMPRE cria um quadro de 5 bytes (40 bits)
        bitsOriginais = 40; 
    } else {
        bitsOriginais = auxiliar.descobrirTotalDeBitsReais(quadro);
    }

    // O array codificado tera o dobro de bits.
    int numeroDeBitsManchester = bitsOriginais * 2;
    int tamanhoArrayManchester = (numeroDeBitsManchester + 31) / 32;
    int[] diferencial = new int[tamanhoArrayManchester];
    
    // O ultimo nivel de sinal. Comecamos com 1 (alto) por escolha propria.
    int ultimoNivel = 1; 

    //for para percorrer os bits e realizar a codificacao de manchester diferencial
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o bit original da posicao 'i'
        int bitOriginal = auxiliar.lerBits(quadro, i, 1);

        // Logica do Manchester Diferencial
        // Se o bit for '0', ha uma transicao no inicio do bit.
        // Se o bit for '1', nao ha transicao.
        if (bitOriginal == 0) {
            ultimoNivel = ultimoNivel ^ 1; // Inverte o nivel do sinal
        }

        // Codificacao Manchester: o primeiro nivel eh o que definimos, 
        // o segundo eh sempre o inverso.
        int bit1 = ultimoNivel;
        int bit2 = ultimoNivel ^ 1;

        // Calcula a posicao dos 2 novos bits no array de destino
        int indiceBit1 = i * 2;
        int indiceBit2 = (i * 2) + 1;

        // Escreve o primeiro bit do par
        auxiliar.escreverBits(diferencial, indiceBit1, bit1, 1);
        
        // Escreve o segundo bit do par
        auxiliar.escreverBits(diferencial, indiceBit2, bit2, 1);
        
        // Atualiza o ultimoNivel para o proximo bit
        ultimoNivel = bit2;
    }
    return diferencial; // retorno da funcao
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraEnquadramentoViolacaoFisica
  * Funcao: Adiciona as flags de inicio e fim (1100) para o enquadramento de violacao da camada fisica.
  * @param quadro | quadro de bits original (SUBQUADRO)
  * @return int[] | novo quadro com as flags
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    String cod = controller.getCodificacao();
    int tipoDeCodificacao = auxiliar.numberCodification(cod);
    final int VIOLACAO = 0b1100;
    final int TAMANHO_VIOLACAO_BITS = 4;

    // A logica original usava 32 bits, o que eh perfeito para nosso subquadro
    final int TAMANHO_SUBQUADRO_EM_BITS = 32; 

    int totalBitsMensagem = auxiliar.descobrirTotalDeBitsReais(quadro);
    if (totalBitsMensagem == 0)
      return new int[0]; // se a mensagem ta vazia nem finaliza o processamento

    // calcula um tamanho MAXIMO estimado para o buffer temporario
    int numSubquadrosEstimado = 1; // So temos 1 subquadro
    int totalBitsSinalEstimado = (TAMANHO_VIOLACAO_BITS * (numSubquadrosEstimado + 1)) + (totalBitsMensagem * 2);
    int[] bufferTemporario = new int[(totalBitsSinalEstimado + 31) / 32];
    int bitEscritaGlobal = 0;

    // escreve a violacao de INICIO (1100)
    auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
    bitEscritaGlobal += TAMANHO_VIOLACAO_BITS; // pula os 4 bits que foram escritos pra violacao

    // codifica os dados da mensagem
    int nivelAtual = 1; // para Manchester Diferencial

    int contadorBitsSubquadro = 0;

    for (int i = 0; i < totalBitsMensagem; i++) {
      int bitOriginal = auxiliar.lerBits(quadro, i, 1);

      if (tipoDeCodificacao == 1 || tipoDeCodificacao == 2) { // Manchester ou Diferencial
        int sinal1, sinal2;
        if (tipoDeCodificacao == 1) { // Manchester
          sinal1 = (bitOriginal == 1) ? 1 : 0;
          sinal2 = (bitOriginal == 1) ? 0 : 1;
        } else { // Manchester Diferencial
          if (bitOriginal == 0)
            nivelAtual = 1 - nivelAtual;
          sinal1 = nivelAtual;
          nivelAtual = 1 - nivelAtual;
          sinal2 = nivelAtual;
        }
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal1, 1);
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal++, sinal2, 1);
      } // fim do if

      contadorBitsSubquadro++;

      // verifica se o quadro acabou ou se eh o fim da mensagem para adiconar a flag 
      boolean ehFimDoSubquadro = (contadorBitsSubquadro == TAMANHO_SUBQUADRO_EM_BITS);
      boolean ehFimDaMensagem = (i == totalBitsMensagem - 1);

      // if para verificar o fim (AGORA SERAO SEMPRE JUNTOS)
      if (ehFimDoSubquadro || ehFimDaMensagem) {
        // Escreve a violacao de FIM de subquadro (que tambem serve como FIM da
        // mensagem)
        auxiliar.escreverBits(bufferTemporario, bitEscritaGlobal, VIOLACAO, TAMANHO_VIOLACAO_BITS);
        bitEscritaGlobal += TAMANHO_VIOLACAO_BITS;
        // Zera o contador para o proximo subquadro
        contadorBitsSubquadro = 0;
      } // fim do if 

    } // fim do for

    // reorganiza o array
    int tamanhoArrayFinal = (bitEscritaGlobal + 31) / 32;
    int[] fluxoBrutoDeBitsFinal = new int[tamanhoArrayFinal];

    // for para copiar apenas os bits validos do buffer temporario para o array final
    for (int i = 0; i < bitEscritaGlobal; i++) {
      int bit = auxiliar.lerBits(bufferTemporario, i, 1);
      auxiliar.escreverBits(fluxoBrutoDeBitsFinal, i, bit, 1);
    } // fim do for

    return fluxoBrutoDeBitsFinal; // retorna o array 
  }// fim do metodo
} // Fim da classe