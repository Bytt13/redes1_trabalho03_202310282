/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 21/08/2025
* Ultima alteracao.: 21/10/2025 (Refatoracao Concorrente)
* Nome.............: CamadaFisicaReceptora
* Funcao...........: Recebe UM subquadro, decodifica, e envia um ACK.
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;
//imports que precisamos

public class CamadaFisicaReceptora {

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
* Metodo: CamadaFisicaReceptora (Construtor Refatorado)
* Funcao: Construtor vazio.
* @param void
* @return void 
* ********************************************************* */
  public CamadaFisicaReceptora() {
    // Vazio. A logica foi movida para 'receberSubquadro'
  }

/**************************************************************
* Metodo: receberSubquadro (NOVO - Antigo Construtor)
* Funcao: decodifica UM subquadro e o passa para camada seguinte
* @param quadro | subquadro codificado e (potencialmente) corrompido
* @return void 
* ********************************************************* */
  public void receberSubquadro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares(); // Cria um objeto para usarmos as funcoes auxiliares
    TelaPrincipalController controller = TelaPrincipalController.getController(); // pega o controller para podermos usar

    String texto = controller.getCodificacao(); // pega o codigo da codificacao escolhida
    int tipoDeCodificacao = auxiliar.numberCodification(texto); // pega a codificacao escolhida e transforma em int
    int[] fluxoBrutoDeBits; // Cria o fluxo de bits que vamos passar adiante (payload decodificado)
    
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
    } // Fim do switch

    // Chama a proxima camada (Enlace Receptora) com o subquadro decodificado
    new CamadaEnlaceDadosReceptora(fluxoBrutoDeBits);

    // Requisito 5: Gerar e enviar um ACK apos processar
    // (independentemente de erro)
    int[] ackQuadro = new int[1]; // 1 int eh suficiente para 8 bits
    int ackBits = 0b10101010; // Padrao de ACK fixo
    
    // Escreve os 8 bits de ACK no int
    auxiliar.escreverBits(ackQuadro, 0, ackBits, 8);
    
    // Envia o ACK de volta pelo Meio
    meio.enviarAck(ackQuadro);

  } // Fim do metodo receberSubquadro

  /*
   * =================================================================
   * OS METODOS ABAIXO (Decodificacoes)
   * PERMANECEM OS MESMOS (sao chamados por 'receberSubquadro')
   * =================================================================
   */
   
  // ... (Metodos CamadaFisicaReceptoraDecodificacao... e DesenquadramentoViolacaoFisica... permanecem inalterados) ...
  // ... (Pequenas correcoes em Manchester para usar lerBits/escreverBits) ...
  
  /**************************************************************
  * Metodo: CamadaFisicaTransmissoraCodificacaoBinaria
  * Funcao: envia a mensagem (em bits) decodificada em binario para a proxima camada
  * @param  quadro | mensagem recebida (em bits)
  * @return a mensagem eh igual aos bits em binario 
  * ********************************************************* */
  private int[] CamadaFisicaReceptoraDecodificacaoBinaria(int[] quadro) {
    return quadro; // Em binario, os bits ja estao na forma final
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDecodificacaoManchester
  * Funcao: envia a mensagem (em bits) decodificada em manchester para a proxima camada
  * @param quadro | mensagem recebida (em bits) (SUBQUADRO)
  * @return a mensagem decodificada em manchester
  * ********************************************************* */
  private int[] CamadaFisicaReceptoraDecodificacaoManchester(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    //if para verificar se foi violacao da camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(quadro);
    } // fim do if
    
    int bitsCodificados;
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        bitsCodificados = 80; // 40 bits originais * 2 = 80 bits Manchester
    } else {
        // Logica antiga
        bitsCodificados = auxiliar.descobrirTotalDeBitsReais(quadro);
        // Garante que o numero de bits seja par para a decodificacao
        if(bitsCodificados % 2 != 0) bitsCodificados++;
    }
    int bitsOriginais = bitsCodificados / 2; // O resultado tera metade dos bits
    
    int tamanhoArrayDecodificado = (bitsOriginais + 31) / 32;
    int[] decodificado = new int[tamanhoArrayDecodificado];

    //for para percorrer os bits e realizar a decodificacao em manchester
    for (int i = 0; i < bitsOriginais; i++) {
        //Define a posicao do par de bits que vamos ler no quadro codificado.
        //Nos so precisamos do primeiro bit do par, que esta na posicao i*2.
        int indiceCodificado = i * 2;
        
        //Le o primeiro bit do par, que eh sempre o bit original na codificacao Manchester.
        int bitOriginal = auxiliar.lerBits(quadro, indiceCodificado, 1);

        //Grava o bit original no array decodificado
        auxiliar.escreverBits(decodificado, i, bitOriginal, 1);
    } // fim do for
    return decodificado; // retorno da funcao
  } // Fim do metodo

  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDecodificacaoManchesterDiferencial
  * Funcao: envia a mensagem (em bits) deodificada em manchester diferencial para a proxima camada
  * @param quadro | mensagem recebida (em bits) (SUBQUADRO)
  * @return a mensagem decodificada em mancheser diferencial
  * ********************************************************* */
  private int[] CamadaFisicaReceptoraDecodificacaoManchesterDiferencial(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    //if para verificar se foi violacao da camada fisica
    if(controller.getEnquadramento().equals("Violacao da Camada Fisica")) {
      return CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(quadro);
    } // fim do if

    int bitsCodificados;
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        bitsCodificados = 80; // 40 bits originais * 2 = 80 bits Manchester
    } else {
        // Logica antiga
        bitsCodificados = auxiliar.descobrirTotalDeBitsReais(quadro);
        // Garante que o numero de bits seja par para a decodificacao
        if(bitsCodificados % 2 != 0) bitsCodificados++;
    }
    int bitsOriginais = bitsCodificados / 2; // O resultado tera metade dos bits

    int tamanhoArrayDecodificado = (bitsOriginais + 31) / 32;
    int[] decodificado = new int[tamanhoArrayDecodificado];

    // O ultimo nivel de sinal que vimos. Comecamos com 1 por escolha (deve ser o mesmo do transmissor).
    int ultimoNivel = 1;

    //for para percorrer os bits e realizar a decodificacao de manchester diferencial
    for (int i = 0; i < bitsOriginais; i++) {
        // Pega o par de bits codificados
        int indiceGeralBit1 = i * 2;
        int primeiroNivelDoPar = auxiliar.lerBits(quadro, indiceGeralBit1, 1);
        
        int indiceGeralBit2 = (i * 2) + 1;
        int segundoNivelDoPar = auxiliar.lerBits(quadro, indiceGeralBit2, 1);

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
        
        //Grava o bit original no array decodificado
        auxiliar.escreverBits(decodificado, i, bitOriginal, 1);

        // O "ultimo nivel" para a proxima iteracao sera o segundo nivel do par atual.
        ultimoNivel = segundoNivelDoPar;
    } // fim do for
    return decodificado; // retorno da funcao
  } // Fim do metodo
  
  /**************************************************************
  * Metodo: CamadaFisicaReceptoraDesenquadramentoViolacaoFisica
  * Funcao: Remove as flags de inicio e fim (1100) do enquadramento de violacao da camada fisica.
  * @param quadro | quadro de bits com as flags (SUBQUADRO)
  * @return int[] | novo quadro sem as flags
  * ********************************************************* */
  private int[] CamadaFisicaReceptoraDesenquadramentoViolacaoFisica(int[] quadro) {
    TelaPrincipalController controller = TelaPrincipalController.getController();
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    String cod = controller.getCodificacao();
    int tipoDeDecodificacao = auxiliar.numberCodification(cod);
    final int VIOLACAO = 0b1100;
    final int TAMANHO_VIOLACAO_BITS = 4;

    int totalBitsSinal = auxiliar.descobrirTotalDeBitsReais(quadro);
    if (totalBitsSinal == 0)
      return new int[0];

    // O payload tera no maximo (totalBitsSinal / 2) bits
    int[] quadroDecodificado = new int[quadro.length]; 
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
          if (!quadroIniciado) {
            quadroIniciado = true; // marca que o processamento de dados pode comear
          } else {
            // Se ja estava iniciado, encontramos a FLAG de fim.
             break; // Encerra o processamento deste subquadro
          }
          i += TAMANHO_VIOLACAO_BITS; // pula os 4 bits da violacao
          continue; // volta ao inicio do loop
        } // fim do if
      } // fim do if

      // if para verificar se o quadro ainda nao foi iniciado
      if (!quadroIniciado) {
        i++; // vai avancando
        continue;
      } // fim do if

      // decodifica os dados (Garante que ainda ha 2 bits para ler)
      if (i + 1 >= totalBitsSinal) break; 

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
    if (tamanhoFinalArray == 0 && bitEscritaGlobal > 0) tamanhoFinalArray = 1; // Garante array de 1 se houver bits
    int[] resultadoFinal = new int[tamanhoFinalArray];
    for (int j = 0; j < bitEscritaGlobal; j++) {
      int bit = auxiliar.lerBits(quadroDecodificado, j, 1);
      auxiliar.escreverBits(resultadoFinal, j, bit, 1);
    }
    return resultadoFinal;


  }// fim do metodo
} // fim da classe