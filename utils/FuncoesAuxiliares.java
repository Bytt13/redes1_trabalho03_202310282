/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 18/08/2025
* Ultima alteracao.: 31/10/2025
* Nome.............: FuncoesAuxiliares
* Funcao...........: Codifica e decodifica bits, e transformas arrays em strings
*************************************************************** */
package utils;

import controller.TelaPrincipalController;

public class FuncoesAuxiliares {
  /**************************************************************
  * Metodo: arrayToString
  * Funcao: transforma o array de bits em uma string para ser apresentada na caixa de texto
  * @param array | bits
  * @return String | string do array de bits
  * ********************************************************* */
  public String arrayToString(int[] array) {
    StringBuilder builder = new StringBuilder(); //Cria o builder da string
    //Loop para transformar os bits do array em string
    for(int bit : array)
    {
      builder.append(bit);
    } // Fim do for
    return builder.toString(); // retorno da funcao
  } // Fim do metodo
  
  /**************************************************************
  * Metodo: binaryArrayToString
  * Funcao: transforma o array de bits em uma string para ser apresentada na caixa de texto
  * @param frotaDeBits | bits
  * @param comprimentoOriginal | tamanho original da string
  * @return String | string do array de bits
  * ********************************************************* */
  public String binaryArrayToString(int[] frotaDeBits, int comprimentoOriginal) {
    StringBuilder palavraDesmontada = new StringBuilder(comprimentoOriginal);

    // Percorre do primeiro ao ultimo caractere original
    for (int i = 0; i < comprimentoOriginal; i++) {
      // Usa a funcao que ja tinhamos para pegar um caractere especifico
      char caractere = getCharFromString(frotaDeBits, i);
      
      // Se encontramos o caractere nulo, ele e o padding (preenchimento).
      // Paramos de ler para nao adicionar lixo na string final.
      if (caractere == '\0') {
          break;
      }

      // Adiciona o caractere recuperado a nossa nova string
      palavraDesmontada.append(caractere);
    }

    return palavraDesmontada.toString(); // retorno da funcao
  } // fim do metodo

  /**************************************************************
  * Metodo: stringToBinary
  * Funcao: transforma o texto em binario
  * @param texto | texto que vai ser convertido
  * @return int[] | array de inteiros do texto em binario
  * ********************************************************* */
  public int[] stringToBinary(String texto) {
    if (texto.isEmpty()) {
      return new int[0]; // Retorna um array vazio se o texto for vazio
  }
  // Calcula quantos inteiros (blocos de 32 bits) serao necessarios
  int numInts = (texto.length() + 3) / 4;
  int[] binario = new int[numInts];
  int charIndex = 0; // Um contador para saber qual caractere do texto estamos lendo

  // Loop para preencher cada inteiro do array 'binario'
  for (int i = 0; i < numInts; i++) {
      int temp = 0; // Variavel temporaria para construir o inteiro
      // Loop para agrupar 4 caracteres (ou o que sobrou) em um inteiro
      for (int j = 0; j < 4; j++) {
          if (charIndex < texto.length()) {
              // Pega o proximo caractere, empurra o temporario 8 bits para a esquerda
              // e adiciona o novo caractere
              temp = (temp << 8) | texto.charAt(charIndex);
              charIndex++;
          } else {
              // Se acabaram os caracteres, completa o restante do inteiro
              // com bits 0 para garantir o alinhamento a esquerda.
              temp = temp << 8;
          }
      }
      binario[i] = temp; // Guarda o inteiro completo no array
  }
  return binario; // Retorno da funcao
  } // Fim do metodo
  /**************************************************************
  * Metodo: binaryString
  * Funcao: retorna uma string de binario
  * @param numero | numero recebido
  * @return String | string dos binarios
  * ********************************************************* */
  public String binaryString(int numero) {
    StringBuilder sb = new StringBuilder(35); // 32 bits + 3 espacos
    for (int i = 31; i >= 0; i--) {
      int bit = (numero >> i) & 1;
      sb.append(bit);
      if (i % 8 == 0 && i > 0) {
        sb.append(" ");
      }
    }
    return sb.toString(); // retorno da funcao
  } // fim do metodo

  /**************************************************************
  * Metodo: getCharFromString
  * Funcao: transforma o inteiro em binario
  * @param bits | os bits recebidos
  * @param i | indice do caractere
  * @return char | letra correspondente
  * ********************************************************* */
  public char getCharFromString(int[] bits, int i) {
    int indice = i / 4; // cria um indice
    int fluxo = bits[indice]; // pega o bit daquele indice especifico
    int pos = i % 4; // Descobre a posicao do caractere dentro do array de int
    int shift = (3 - pos) * 8; //calcula o deslocamento

    return (char) ((fluxo >> shift) & 255); // aplica a mascara 11111111 (255) para limpar os outros bits
  } // fim do metodo

  /**************************************************************
* Metodo: lerBits
* Funcao: le uma sequencia especifica de bits de um array de inteiros
* @param array | os bits recebidos
* @param posInicialBit | posicao inicial do bit
* @param numBits | numero de bits a ler
* @return int | o valor inteiro formado pelos bits lidos
* ********************************************************* */
public int lerBits(int[] array, int posInicialBit, int numBits) {
   int valorLido = 0; // o valor que sera retornado

    for (int i = 0; i < numBits; i++) {
      int posicaoGlobal = posInicialBit + i; // calcula a posicao global do bit a ser lido
      int indiceDoPacote = posicaoGlobal / 32; // descobre em qual inteiro do array sera lido
      int posicaoNoPacote = 31 - (posicaoGlobal % 32); // calcula a posicao dentro do pacote

      int bitLido = (array[indiceDoPacote] >> posicaoNoPacote) & 1; // extrai o bit da posicao correta

      // se o bit lido for 1, o posiciona corretamente no valor de retorno
      if (bitLido == 1) {
        valorLido = valorLido | (1 << (numBits - 1 - i));
      }
    } // fim for
    return valorLido; // retorna o valor lido
} // fim do metodo

  /**************************************************************
  * Metodo: escreverBits
  * Funcao: escreve um valor inteiro como uma sequencia de bits em um array
  * @param array | os bits recebidos
  * @param posInicialBit | posicao inicial para escrita
  * @param valor | valor a ser escrito em bits
  * @param numBits | numero de bits para escrever
  * @return void
  * ********************************************************* */
  public void escreverBits(int[] array, int posInicialBit, int valor, int numBits) {
    for (int i = 0; i < numBits; i++) {
          // Extrai o i-esimo bit mais significativo do 'valor'
          int bitParaEscrever = (valor >> (numBits - 1 - i)) & 1;

          // Se o bit for 0, nao fazemos nada (assumindo que o array comeca zerado).
          // Se for 1, precisamos escreve-lo.
          if (bitParaEscrever == 1) {
              int posicaoGlobal = posInicialBit + i;   // Calcula a posicao global do bit
              int indiceInt = posicaoGlobal / 32;      // Descobre em qual 'int' do array o bit vai
              int posNoInt = 31 - (posicaoGlobal % 32); // Descobre a posicao dentro desse 'int'

              // Usa o operador OU para ligar o bit na posicao correta
              // sem alterar os outros bits.
              array[indiceInt] = array[indiceInt] | (1 << posNoInt);
          }
      }
  } // fim do metodo

  /**************************************************************
  * Metodo: arrayDeBitsParaString
  * Funcao: Converte um array de inteiros (bits) em uma string binaria,
  * limitada a um numero especifico de bits, para exibicao na GUI.
  * @param arrayDeBits | O array de inteiros contendo os bits.
  * @param totalDeBitsParaMostrar | O numero exato de bits a serem convertidos.
  * @return String | String formatada dos bits.
  * ********************************************************* */
  public String arrayDeBitsParaString(int[] arrayDeBits, int totalDeBitsParaMostrar) {
      StringBuilder builder = new StringBuilder();
      int bitsMostrados = 0;

      // Itera sobre cada inteiro no array
      for (int i = 0; i < arrayDeBits.length; i++) {
          int numero = arrayDeBits[i];
          // Itera sobre cada bit do inteiro (do mais significativo ao menos)
          for (int j = 31; j >= 0; j--) {
              if (bitsMostrados >= totalDeBitsParaMostrar) {
                  break; // Para se ja mostramos todos os bits necessarios
              }

              int bit = (numero >> j) & 1;
              builder.append(bit);
              bitsMostrados++;

              // Adiciona um espaco a cada 8 bits para legibilidade
              if (bitsMostrados % 8 == 0 && bitsMostrados > 0 && bitsMostrados < totalDeBitsParaMostrar) {
                  builder.append(" ");
              }
          }
          if (bitsMostrados >= totalDeBitsParaMostrar) {
              break;
          }
      }
      return builder.toString();
  }// fim do metodo
  /**************************************************************
  * Metodo: numberCodification
  * Funcao: transforma o tipo de codificacao escolhida em numero
  * @param c | codificacao escolhida (String)
  * @return int | numero correspondente
  * ********************************************************* */
  public int numberCodification(String c) {
    int number = 0; // numero que vai receber a codificacao
    // Switch que vai permitir que o numero seja escolhido dependendo do que foi escolhido no comboBox
    switch(c) {
      case "Binario":
        number = 0;
        break;
      case "Manchester":
        number = 1;
        break;
      case "Manchester Diferencial":
        number = 2;
        break;
      default:
        number = 0;
        break;
    }
    return number; // Retorno do numero correspondente
  } // Fim do metodo

  /**************************************************************
  * Metodo: enquadCodification
  * Funcao: transforma o tipo de enquadramento escolhido em numero
  * @param c | enquadramento escolhido (String)
  * @return int | numero correspondente
  * ********************************************************* */
  public int enquadCodification(String c) {
    int number = 0; // numero que vai receber a codificacao
    // Switch que vai permitir que o numero seja escolhido dependendo do que foi escolhido no comboBox
    switch(c) {
      case "Contagem de Caracteres":
        number = 0;
        break;
      case "Insercao de bytes":
        number = 1;
        break;
      case "Insercao de bits":
        number = 2;
        break;
      case "Violacao da Camada Fisica":
        number = 3;
        break;
      default:
        number = 0;
        break;
    }
    return number; // Retorno do numero correspondente
  } // Fim do metodo

  /**************************************************************
  * Metodo: animate
  * Funcao: anima a onda de transmissao na GUI
  * @param controller | controller para animar
  * @param fluxoBrutoDeBits | fluxo de bits que vamos animar
  * @param totalDeBitsParaAnimar | numero total de bits para animar
  * @return void
  * *********************************************************** */
  public void animate(TelaPrincipalController controller, int[] fluxoBrutoDeBits, int totalDeBitsParaAnimar) {
  int[] bitsParaAnimacao = controller.desempacotarBitsParaAnimacao(fluxoBrutoDeBits, totalDeBitsParaAnimar);
  controller.drawSignal(bitsParaAnimacao); // desenha a animacao
  } // fim do metodo
   /**************************************************************
  * Metodo: descobrirTotalDeBitsReais
  * Funcao: descobre os bits uteis
  * @param quadro | array de inteiros com os bits
  * @return int | posicao do ultimo bit 1
  * ********************************************************* */
  public int descobrirTotalDeBitsReais(int[] quadro) {
    // if para verificar se o parametro eh nulo
    if (quadro == null || quadro.length == 0) { 
      return 0;
    } // fim do if

    // for para descobrir o total de bits reais
    for (int i = (quadro.length * 32) - 1; i >= 0; i--) {
      // if para verificar se os bits uteis acabaram
      if (lerBits(quadro, i, 1) == 1) {
        return i + 1;
      } // fim do if
    } // fim do for

    return 0; // retorno da funcao caso nada acima aconteca
  } // fim do metodo
    /**************************************************************
  * Metodo: controlCodification
  * Funcao: transforma o tipo de controle escolhido em numero
  * @param c | controle escolhido (String)
  * @return int | numero correspondente
  * ********************************************************* */
  public int controlCodification(String c) {
    int number = 0; // numero que vai receber a codificacao
    // Switch que vai permitir que o numero seja escolhido dependendo do que foi escolhido no comboBox
    switch(c) {
      case "Bit de Paridade par":
        number = 0;
        break;
      case "Bit de paridade impar":
        number = 1;
        break;
      case "CRC":
        number = 2;
        break;
      case "Codigo de Hamming":
        number = 3;
        break;
      default:
        number = 0;
        break;
    }
    return number; // Retorno do numero correspondente
  } // Fim do metodo

  /**************************************************************
  * Metodo: controlCodification
  * Funcao: transforma o tipo de controle escolhido em numero
  * @param subquadro | subquadro passado
  * @param numseq | numero de sequencia
  * @return int | numero correspondente
  * ********************************************************* */
  public int[] ordenarQuadro(int[] subquadro, int numseq) {
    // 1. Descobrir o tamanho real (em bits) do subquadro recebido.
    // Usamos 'this' para chamar outro metodo da mesma classe.
    int totalBitsDados = this.descobrirTotalDeBitsReais(subquadro);

    // 2. Definir o tamanho do novo quadro.
    final int TAMANHO_CABECALHO_BITS = 8;
    int novoTotalBits = TAMANHO_CABECALHO_BITS + totalBitsDados;

    // 3. Alocar o novo array de inteiros.
    int novoTamanhoInts = (novoTotalBits + 31) / 32;
    int[] quadroOrdenado = new int[novoTamanhoInts];

    // 4. Escrever o cabecalho (numero de sequencia) de 8 bits.
    // Escreve o 'numseq' na posicao 0, usando 8 bits.
    // Validamos para garantir que o numseq nao estoure 8 bits (0-255)
    this.escreverBits(quadroOrdenado, 0, numseq & 0xFF, TAMANHO_CABECALHO_BITS);

    // 5. Copiar os bits de dados do subquadro original para o novo quadro.
    // Comecamos a escrever na posicao 8.
    for (int i = 0; i < totalBitsDados; i++) {
        // Le o bit 'i' do subquadro
        int bit = this.lerBits(subquadro, i, 1);
        // Escreve o bit 'i' na posicao do novo quadro
        this.escreverBits(quadroOrdenado, i + TAMANHO_CABECALHO_BITS, bit, 1);
    }

    // 6. Retornar o novo quadro (Cabecalho + Dados).
    return quadroOrdenado;
  } // fim do metodo

  public int[] organizarQuadro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    // 1. Descobrir o tamanho total do quadro recebido.
    int totalBitsRecebidos = auxiliar.descobrirTotalDeBitsReais(quadro);
    final int TAMANHO_CABECALHO_BITS = 8;

    // 2. Validar se o quadro tem pelo menos o tamanho do cabecalho.
    if (totalBitsRecebidos < TAMANHO_CABECALHO_BITS) {
      System.out.println("Erro de Fluxo: Quadro recebido menor que o cabecalho de sequencia.");
      return new int[0]; // Retorna um quadro vazio
    }

    // 3. Ler o numero de sequencia (os 8 primeiros bits).
    int numSeq = auxiliar.lerBits(quadro, 0, TAMANHO_CABECALHO_BITS);
    
    // Log para depuracao (opcional)
    System.out.println("[FLUXO RX] Recebido quadro com SeqNum = ");
    System.out.println(numSeq);

    // 4. Calcular o tamanho do quadro de dados (payload).
    int totalBitsDados = totalBitsRecebidos - TAMANHO_CABECALHO_BITS;
    if (totalBitsDados <= 0) {
      return new int[0]; // Nao ha dados, apenas cabecalho
    }

    // 5. Alocar o novo array para os dados.
    int novoTamanhoInts = (totalBitsDados + 31) / 32;
    int[] quadroPayload = new int[novoTamanhoInts];

    // 6. Copiar os bits de dados (pulando o cabecalho).
    for (int i = 0; i < totalBitsDados; i++) {
        // Le o bit da posicao 'i + 8' do quadro original
        int bit = auxiliar.lerBits(quadro, i + TAMANHO_CABECALHO_BITS, 1);
        // Escreve o bit na posicao 'i' do novo quadro (payload)
        auxiliar.escreverBits(quadroPayload, i, bit, 1);
    }

    return quadroPayload;
  } // fim do metodo
} // fim da classe