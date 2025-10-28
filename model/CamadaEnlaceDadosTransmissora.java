/*****************************************************************
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 16/09/2025
* Ultima alteracao.: 27/09/2025
* Nome.............: CamadaEnlaceDadosTransmissora
* Funcao...........: Transfere a mensagem enquadrada para camada fisica transmissora
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;


public class CamadaEnlaceDadosTransmissora {
  /**************************************************************
  * Metodo: CamadaEnlaceDadosTransmissora
  * Funcao: enquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  public CamadaEnlaceDadosTransmissora(int []quadro) {
    int[] quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramento(quadro);
    int[] quadroControlado = CamadaDeEnlaceTransmissoraControleDeErro(quadroEnquadrado);
    int[] quadroOrdenado = CamadaDeEnlaceTransmissoraControleDeFluxo(quadroControlado);

    new CamadaFisicaTransmissora(quadroOrdenado);
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramento
  * Funcao: enquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramento(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeEnquadramento = auxiliar.enquadCodification(controller.getEnquadramento()); // enquadramento escolhido
    int[] quadroEnquadrado; // quadro depois de ser enquadrado
    // Switch para escolher a codificacao escolhida
    switch(tipoDeEnquadramento) {
      case 0: //contagem de caracteres
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: //insercao de bytes
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: //insercao de bits
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: //violacao da camada fisica
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
      default:
        quadroEnquadrado = CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(quadro);
        break;
    } // Fim do switch

    return quadroEnquadrado;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraControleDeErro
  * Funcao: faz o controle de erros dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraControleDeErro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeControle = auxiliar.controlCodification(controller.getControleErro()); // pega o controle de erro escolhido
    int[] quadroControlado; // quadro depois de passar pelo controle de erros
    //switch para pegar o controle de erro 
    switch(tipoDeControle) {
      case 0: //bit de paridade par
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: //bit de paridade impar
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: //CRC
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroCRC(quadro);
        break;
      case 3: //codigo de hamming
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming(quadro);
        break;
      default:
        quadroControlado = CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(quadro);
        break;
    } // Fim do switch

    return quadroControlado;
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraControleDeFluxo
  * Funcao: faz o controle de fluxo dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraControleDeFluxo(int[] quadro) {
    return quadro;
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoContagemDeCaracteres
  * Funcao: enquadra os bits (Contagem de Caracteres) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoContagemDeCaracteres(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    // quantos caracteres vamos contar, por agora, sera em bytes, mas a funcao manipula bits
    final int caracteres = 4;
    // quanto o cabecalho ocupa, 1 byte, ou seja, 8 bits
    final int cabecalho = 1;
    // valor do controle
    final int controle = caracteres + cabecalho;
    // agora sim, tudo em bits
    final int frameBits = controle * 8;

    //calcula o tamanho necessario para o novo array
    int frames = quadro.length;
    int tamanhoBits = frames * frameBits;
    int tamanhoInts = (tamanhoBits + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoInts];
    // ponteiro que escreve o bit
    int ptr = 0;
    // for para aplicarmos a contagem de caracteres
    for(int i = 0; i < quadro.length; i++) {
      //escreve 5
      int carga = quadro[i];
      auxiliar.escreverBits(quadroEnquadrado, ptr, controle, 8);
      ptr += 8; // avanca o ponteiro
      //escreve o controle depois de escrever o cabecalho (apos os 4 caracteres)
      auxiliar.escreverBits(quadroEnquadrado, ptr, carga, 32);
      ptr += 32;
    } // fim do for

    return quadroEnquadrado; //retorno da funcao
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoInsercaoDeBytes
  * Funcao: enquadra os bits (Insercao de Bytes) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBytes(int[] quadro) {
    final int FLAG = 0b01111110; // ~ em ASCII
    final int ESC = 0b01111101; // } em ASCII
    final int TAMANHO_SUBQUADRO_BYTES = 4; // de quantos em quantos bytes vai adicionar o flag

    //calcular o tamanho do array
    int tamanhoFinal = 2; // comeca em 2 por causa das FLAGs
    int contadorByteFlagIntermediaria = 0; // Contador para adicionar a flag intermediaria
    // for para desempacotar o inteiro
    for(int inteiroBruto : quadro) {
      if(inteiroBruto == 0) break;
      // Manipula os bits para analise
      int[] intBit = new int[4];
      // realiza as operacoes bitwise para manipular o inteiro e transformar em bits
      intBit[0] = (inteiroBruto >> 24) & 0xFF;
      intBit[1] = (inteiroBruto >> 16) & 0xFF;
      intBit[2] = (inteiroBruto >> 8) & 0xFF;
      intBit[3] = (inteiroBruto) & 0xFF;

      //for para calcular o tamanho do array
      for(int intBitAtual : intBit) {
        if(intBitAtual == 0) continue; //ignora bytes "nulos"
        //if para ver em quanto vai aumentar
        if(intBitAtual == FLAG || intBitAtual == ESC) {
          tamanhoFinal += 2;
        } else {
          tamanhoFinal++;
          contadorByteFlagIntermediaria++;
        } // fim do if

        // if para adicioar espaco para FLAG intermediaria
        if (contadorByteFlagIntermediaria == TAMANHO_SUBQUADRO_BYTES) {
          tamanhoFinal++; // Adiciona espaco para a FLAG intermediaria
          contadorByteFlagIntermediaria = 0; // Zera o contador
        } // fim do if
      } // fim do for
    } // fim do for

    int[] enquadrado = new int[tamanhoFinal];
    int ptr = 0;
    enquadrado[ptr++] = FLAG; // adiciona a FLAG de inicio

    contadorByteFlagIntermediaria = 0;

    //preencher o array
    //for para pegar os bits
    for(int inteiroBruto : quadro) {
      if(inteiroBruto == 0) break;
      // Manipula os bits para analise
      int[] intBit = new int[4];
      // realiza as operacoes bitwise para manipular o inteiro e transformar em bits
      intBit[0] = (inteiroBruto >> 24) & 0xFF;
      intBit[1] = (inteiroBruto >> 16) & 0xFF;
      intBit[2] = (inteiroBruto >> 8) & 0xFF;
      intBit[3] = (inteiroBruto) & 0xFF;

      //for para calcular o tamanho do array
      for(int intBitAtual : intBit) {
        if(intBitAtual == 0) continue; //ignora bytes "nulos"
        //if para ver em quanto vai aumentar
        if(intBitAtual == FLAG || intBitAtual == ESC) {
          enquadrado[ptr++] = ESC;
          enquadrado[ptr++] = intBitAtual;
        } else {
          enquadrado[ptr++] = intBitAtual;
          contadorByteFlagIntermediaria++;
        } // fim do if
        //if para escrever a FLAG intermediaria
        if (contadorByteFlagIntermediaria == TAMANHO_SUBQUADRO_BYTES) {
            // if que verifica se a proxima posicao ainda nao eh a ultima (reservada para a FLAG final)
            if (ptr < enquadrado.length -1) {
                enquadrado[ptr++] = FLAG;
                contadorByteFlagIntermediaria = 0;
            } // fim do if
        } // fim do if
      } // fim do for
    } // fim do for

    enquadrado[ptr] = FLAG; // adiciona a flag de fim
    // reempacotar
    int tamanhoInts = (tamanhoFinal+ 3) / 4;
    int[] quadroEnquadrado = new int[tamanhoInts];

    //for para reempacotar o inteiro
    for(int i = 0; i < tamanhoInts; i++) {
      int inteiro = 0;
      // for para manipular os bits
      for(int j = 0; j < 4; j++) {
        int indice = i * 4 + j;
        // if para deslocar os bits
        if(indice < enquadrado.length) {
          int x = enquadrado[indice];
          inteiro |= (x << (24 - j * 8));
        } // fim do if
      } // fim do for
      quadroEnquadrado[i] = inteiro;
    } // fim do for
    return quadroEnquadrado; // retorno da funcao
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoInsercaoDeBits
  * Funcao: enquadra os bits (Insercao de Bits) com flags a cada 32 bits e passa para a proxima camada
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoInsercaoDeBits(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    int totalDeBitsOriginal = quadro.length * 32;
    final int FLAG = 0b01111110;
    final int TAMANHO_BLOCO = 32;
    int tamanhoFinalBits = 0;
    tamanhoFinalBits += 8; // FLAG inicial

    int contadorDeUns = 0;
    for (int i = 0; i < totalDeBitsOriginal; i++) {
        if (i > 0 && i % TAMANHO_BLOCO == 0) {
            tamanhoFinalBits += 8; // Adiciona espaco para a FLAG intermediaria
            contadorDeUns = 0;
        }
        
        int bit = auxiliar.lerBits(quadro, i, 1);
        tamanhoFinalBits++; // Adiciona o bit de dado

        if (bit == 1) {
            contadorDeUns++;
            if (contadorDeUns == 5) {
                tamanhoFinalBits++; // Adiciona espaco para o bit de stuffing
                contadorDeUns = 0;
            }
        } else {
            contadorDeUns = 0;
        }
    }
    tamanhoFinalBits += 8; // FLAG final

    int tamanhoFinalInts = (tamanhoFinalBits + 31) / 32;
    int[] quadroEnquadrado = new int[tamanhoFinalInts];
    int ponteiroEscrita = 0;

    // Escreve a FLAG inicial
    auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);
    ponteiroEscrita += 8;

    contadorDeUns = 0;
    // for para iterar sobre os bits originais, escrevendo dados, flags e fazendo stuffing
    for (int i = 0; i < totalDeBitsOriginal; i++) {
        // if para verificar se completamos um bloco, escreve a FLAG intermediaria
        if (i > 0 && i % TAMANHO_BLOCO == 0) {
            auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);
            ponteiroEscrita += 8;
            contadorDeUns = 0; 
        } // fim do if
        
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita++, bit, 1);

        // if para verificar se eh 1
        if (bit == 1) {
            contadorDeUns++;
            // if para adicionar o 0
            if (contadorDeUns == 5) {
                auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita++, 0, 1);
                contadorDeUns = 0;
            } // fim do if
        } else {
            contadorDeUns = 0;
        } // fim do if-else
    } // fim do for

    // Escreve a FLAG final
    auxiliar.escreverBits(quadroEnquadrado, ponteiroEscrita, FLAG, 8);

    return quadroEnquadrado;
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceTrasnmissoraEnquadramentoViolacaoCamadaFisica
  * Funcao: enquadra os bits (Violacao da Camada Fisica) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceTransmissoraEnquadramentoViolacaoCamadaFisica(int[] quadro) {
    return quadro;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroBitParidadePar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroBitParidadePar(int[] quadro) {
    // Cria uma instancia das funcoes auxiliares
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    // Descobre quantos bits realmente existem na mensagem
    int totalBits = quadro.length * 32;

    // Se a mensagem estiver vazia, apenas retorna o quadro original
    if (totalBits == 0) {
      return quadro;
    }

    // 1. inicializar contador de um
    int contadorDeUns = 0;

    // 2. percorre quadro[] e conta os bits '1'
    for (int i = 0; i < totalBits; i++) {
      if (auxiliar.lerBits(quadro, i, 1) == 1) {
        contadorDeUns++; // 3. adicione 1 ao contador de um
      }
    }
    System.out.println(contadorDeUns);

    // 4. Determina qual deve ser o bit de paridade
    int bitDeParidade;
    // se contador de um % 2 == 0 (paridade ja eh par)
    if (contadorDeUns % 2 == 0) {
      bitDeParidade = 0; // Adicionamos '0' para manter a contagem par
    } else { // senao (paridade eh impar)
      bitDeParidade = 1; // Adicionamos '1' para tornar a contagem par
    }

    // 5. Cria o novo quadro controlado com espaco para +1 bit
    int novoTotalBits = totalBits + 1;
    // Calcula o tamanho do novo array de int[] necessario para armazenar os bits
    int novoTamanhoInts = (novoTotalBits + 31) / 32; 
    
    // 6. inicializar quadro controlado
    int[] quadroControlado = new int[novoTamanhoInts];

    // 7. para cada i em quadro[] faca ler bits em quadro[] e escrever bits lidos em quadro controlado[]
    for (int i = 0; i < totalBits; i++) {
      int bit = auxiliar.lerBits(quadro, i, 1);
      auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    // 8. quadro controlado[ultima posicao] = bitDeParidade
    // A "ultima posicao" eh a de indice 'totalBits' (ja que comecamos do 0)
    auxiliar.escreverBits(quadroControlado, totalBits, bitDeParidade, 1);

    // 9. retorne quadro controlado[]
    return quadroControlado;
  } //fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroBitParidadeImpar(int[] quadro) {
    // Cria uma instancia das funcoes auxiliares
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    // Descobre quantos bits realmente existem na mensagem
    int totalBits = quadro.length * 32;

    // Se a mensagem estiver vazia, apenas retorna o quadro original
    if (totalBits == 0) {
      return quadro;
    }

    // 1. inicializar contador de um
    int contadorDeUns = 0;

    // 2. percorre quadro[] e conta os bits '1'
    for (int i = 0; i < totalBits; i++) {
      if (auxiliar.lerBits(quadro, i, 1) == 1) {
        contadorDeUns++; // 3. adicione 1 ao contador de um
      }
    }

    // 4. Determina qual deve ser o bit de paridade
    int bitDeParidade;
    // se contador de um % 2 != 0 (paridade ja eh impar)
    if (contadorDeUns % 2 != 0) {
      bitDeParidade = 0; // Adicionamos '0' para manter a contagem impar
    } else { // senao (paridade eh par)
      bitDeParidade = 1; // Adicionamos '1' para tornar a contagem impar
    }

    System.out.println(contadorDeUns);
    // 5. Cria o novo quadro controlado com espaco para +1 bit
    int novoTotalBits = totalBits + 1;
    // Calcula o tamanho do novo array de int[] necessario para armazenar os bits
    int novoTamanhoInts = (novoTotalBits + 31) / 32; 
    
    // 6. inicializar quadro controlado
    int[] quadroControlado = new int[novoTamanhoInts];

    // 7. para cada i em quadro[] faca ler bits em quadro[] e escrever bits lidos em quadro controlado[]
    for (int i = 0; i < totalBits; i++) {
      int bit = auxiliar.lerBits(quadro, i, 1);
      auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    // 8. quadro controlado[ultima posicao] = bitDeParidade
    // A "ultima posicao" eh a de indice 'totalBits' (ja que comecamos do 0)
    auxiliar.escreverBits(quadroControlado, totalBits, bitDeParidade, 1);

    // 9. retorne quadro controlado[]
    return quadroControlado;
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroCRC
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas  e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroCRC(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    int totalBits = quadro.length * 32;

    // 1. Calcular o CRC baseado no pseudocodigo
    final int POLY = 0x04C11DB7;
    final int INIT = 0xFFFFFFFF;
    final int XOROUT = 0xFFFFFFFF;
    int reg = INIT;

    // Processar bits da mensagem
    for (int i = 0; i < totalBits; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1); 
        int top_bit = (reg >>> 31) & 1; // Pega o MSB (usa shift logico)
        reg = (reg << 1) | bit;       // Desloca e injeta o bit da mensagem
        
        if (top_bit == 1) {
            reg = reg ^ POLY;
        }
    }

    // Processar os 32 bits '0' virtuais (conforme implicito no pseudocodigo)
    for (int i = 0; i < 32; i++) {
        int top_bit = (reg >>> 31) & 1;
        reg = (reg << 1) | 0; // Desloca e injeta um bit '0'
        
        if (top_bit == 1) {
            reg = reg ^ POLY;
        }
    }

    int crc = reg ^ XOROUT; // Etapa final de XOR

    // 2. Criar o novo quadro com o CRC anexado
    int novoTotalBits = totalBits + 32;
    int novoTamanhoInts = (novoTotalBits + 31) / 32; 
    int[] quadroControlado = new int[novoTamanhoInts];

    // 3. Copiar dados originais
    for (int i = 0; i < totalBits; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    // 4. Anexar o CRC de 32 bits no final
    // A funcao escreverBits ja lida com a ordem MSB-first
    auxiliar.escreverBits(quadroControlado, totalBits, crc, 32);

    return quadroControlado;
  } // fim do metodo
    /**************************************************************
  * Metodo: CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro, ou pelo menos controladas e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceTransmissoraControleDeErroCodigoDeHamming(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    int totalDataBits = quadro.length * 32;

    if (totalDataBits == 0) {
      return new int[0];
    }

    // 1. Descobrir quantos bits de paridade (r) sao necessarios
    int r = 0;
    // A regra eh: 2^r >= d + r + 1 (onde d = totalDataBits)
    while ((1 << r) < (totalDataBits + r + 1)) {
      r++;
    }

    int totalHammingBits = totalDataBits + r;
    int novoTamanhoInts = (totalHammingBits + 31) / 32;
    int[] quadroControlado = new int[novoTamanhoInts];

    // 2. Posicionar os bits de DADOS (d) nas posicoes que NAO sao potencia de 2
    int ponteiroDados = 0; // Ponteiro para ler do 'quadro' original
    // O algoritmo de Hamming eh 1-based (comeca em 1)
    for (int pos = 1; pos <= totalHammingBits; pos++) {
      // (pos & (pos - 1)) == 0 eh um truque para verificar se 'pos' eh potencia de 2
      if ((pos & (pos - 1)) == 0) {
        // Pula as posicoes de paridade (1, 2, 4, 8, 16...)
        continue;
      }

      // Se nao for potencia de 2, eh uma posicao de dado
      if (ponteiroDados < totalDataBits) {
        int bit = auxiliar.lerBits(quadro, ponteiroDados, 1);
        auxiliar.escreverBits(quadroControlado, pos - 1, bit, 1); // Escreve na posicao 0-based
        ponteiroDados++;
      }
    }

    // 3. Calcular e posicionar os bits de PARIDADE (r)
    // Para cada bit de paridade (p=0 -> pos 1, p=1 -> pos 2, p=2 -> pos 4...)
    for (int p = 0; p < r; p++) {
      int posParidade = 1 << p; // 1, 2, 4, 8, 16...
      int soma = 0; // Usaremos paridade PAR (soma com XOR)

      // Verifica todos os bits que esta paridade cobre
      for (int i = 1; i <= totalHammingBits; i++) {
        // Se o bit 'i' deve ser checado pela paridade 'posParidade'
        if ((i & posParidade) != 0) {
          // Nao podemos incluir o proprio bit de paridade no calculo inicial
          if (i == posParidade) {
            continue;
          }
          int bit = auxiliar.lerBits(quadroControlado, i - 1, 1);
          soma = soma ^ bit;
        }
      }
      // Escreve o bit de paridade calculado na sua posicao
      auxiliar.escreverBits(quadroControlado, posParidade - 1, soma, 1);
    }

    return quadroControlado;
  } // fim do metodo
} // Fim da classe