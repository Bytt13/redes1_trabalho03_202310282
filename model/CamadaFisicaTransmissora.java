/***************************************************************** * Autor............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 19/08/2025
* Ultima alteracao.: 23/10/2025 (Correcao de Paridade e Timeout)
* Nome.............: CamadaFisicaTransmissora
* Funcao...........: Codifica e envia UM subquadro. Recebe ACKs.
*************************************************************** */

package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaFisicaTransmissora {
  
  // Referencia ao "onibus" de comunicacao
  private MeioDeComunicacao meio;
  private volatile boolean ackRecebido = false;
  private final Object ackLock = new Object();
  private static final int TIMEOUT_ACK_MS = 2000; // Timeout de 2 segundos

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
  * @param quadro | subquadro enquadrado e controlado (ex: 41 bits)
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
  
  // --- INICIO DA CORRECAO (HARD-CODED 40 bits) ---
  // Removemos o 'if (enquadramento.equals("Contagem de Caracteres"))'
  // Agora, ele descobre o tamanho real (ex: 82 bits Manchester)
  // dinamicamente, independentemente do metodo.
  totalDeBitsReais = auxiliar.descobrirTotalDeBitsReais(fluxoBrutoDeBits);
  // --- FIM DA CORRECAO ---


  // Agora usamos a funcao auxiliar correta para criar a string
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
    // Reseta a flag de ACK antes de enviar
    this.ackRecebido = false; 
    
    // Envia o subquadro codificado para o Meio
    meio.transferir(fluxoBrutoDeBits);

    // Espera pelo ACK
    synchronized (ackLock) {
        if (!ackRecebido) {
            try {
                // Espera pelo tempo de TIMEOUT
                ackLock.wait(TIMEOUT_ACK_MS); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println(Thread.currentThread().getName());
                System.out.println("stopped");
            }
        }
    }

    // Se nao recebeu o ACK apos o tempo, registra o timeout
    if (!ackRecebido) {
        System.out.println("TIMEOUT: ACK nao recebido para ");
        System.out.println(Thread.currentThread().getName());
        // Aqui entraria a logica de RETRANSMISSAO.
        // Por enquanto, apenas registramos o timeout e a thread morre.
    }
    // --- FIM DA LOGICA DE TIMEOUT ---
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
        synchronized (ackLock) {
            this.ackRecebido = true;
            ackLock.notifyAll(); // Acorda a thread que esta esperando em enviarSubquadro
        }
        // --- FIM DA LOGICA DE TIMEOUT ---
        System.out.println("ACK Recebido pela Thread: ");
        System.out.println(Thread.currentThread().getName());

    } else {
        System.out.println("Quadro de resposta desconhecido recebido.");
    }
  } // Fim do metodo receberAck

  /*
   * =================================================================
   * OS METODOS ABAIXO (Codificacoes)
   * =================================================================
   */
   
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
  * @param quadro | mensagem recebida (em bits) (SUBQUADRO, ex: 41 bits)
  * @return a mensagem codificada em manchester (ex: 82 bits)
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchester(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    
    // --- INICIO DA CORRECAO (HARD-CODED 40 bits) ---
    // Descobre o nro de bits REAIS no subquadro (pode ser 40, 41 com paridade, etc.)
    int bitsOriginais = auxiliar.descobrirTotalDeBitsReais(quadro); 
    // --- FIM DA CORRECAO ---
    
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
  * @param  quadro | mensagem recebida (em bits) (SUBQUADRO, ex: 41 bits)
  * @return a mensagem codificada em manchester diferencial (ex: 82 bits)
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraCodificacaoManchesterDiferencial(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    // if para verificar se precisamos usar a violacao de camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(quadro);
    } // fim do if
    
    // --- INICIO DA CORRECAO (HARD-CODED 40 bits) ---
    int bitsOriginais = auxiliar.descobrirTotalDeBitsReais(quadro);
    // --- FIM DA CORRECAO ---

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
  * @param quadro | quadro de bits original (SUBQUADRO, ex: 33 bits)
  * @return int[] | novo quadro com as flags
  * ********************************************************* */
  private int[] CamadaFisicaTransmissoraEnquadramentoViolacaoFisica(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    String cod = controller.getCodificacao();
    int tipoDeCodificacao = auxiliar.numberCodification(cod);
    final int VIOLACAO = 0b1100;
    final int TAMANHO_VIOLACAO_BITS = 4;

    int totalBitsMensagem = auxiliar.descobrirTotalDeBitsReais(quadro);
    if (totalBitsMensagem == 0)
      return new int[0]; // se a mensagem ta vazia nem finaliza o processamento

    // --- INICIO DA CORRECAO (HARD-CODED 32 bits) ---
    // A logica original usava 32 bits, mas agora deve usar o tamanho real
    // (ex: 33 bits com paridade)
    final int TAMANHO_SUBQUADRO_EM_BITS = totalBitsMensagem; 
    // --- FIM DA CORRECAO ---

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
      // Com a correcao, ehFimDoSubquadro e ehFimDaMensagem serao verdadeiros juntos
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