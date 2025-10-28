/***************************************************************** * Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 16/09/2025
* Ultima alteracao.: 21/10/2025 (Refatoracao Concorrente)
* Nome.............: CamadaEnlaceDadosReceptora
* Funcao...........: Transfere a mensagem decodificada e desenquadrada para camada aplicacao receptora
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import utils.FuncoesAuxiliares;

public class CamadaEnlaceDadosReceptora {
  private int[] quadroVerificado; // O payload (se limpo) ou null (se erro)
  private boolean quadroEstaLimpo = false;
  /**************************************************************
  * Metodo: CamadaEnlaceDadosReceptora
  * Funcao: desenquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  // Este é o construtor que voce esta usando (da refatoracao de pipeline)
  public CamadaEnlaceDadosReceptora(int[] quadro) {
    this.quadroVerificado = CamadaDeEnlaceReceptoraControleDeErro(quadro);
    
    // 2. ARMAZENA O RESULTADO
    if (this.quadroVerificado != null) {
        this.quadroEstaLimpo = true;
    } else {
        this.quadroEstaLimpo = false;
    }

    /*int[] quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramento(quadro);
    int[] quadroControlado = CamadaDeEnlaceReceptoraControleDeErro(quadroDesenquadrado);
    CamadaDeEnlaceReceptoraControleDeFluxo(quadroControlado);

    new CamadaDeAplicacaoReceptora(quadroDesenquadrado);*/
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramento
  * Funcao: desenquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroEnquadrado 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramento(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeEnquadramento = auxiliar.enquadCodification(controller.getEnquadramento()); // enquadramento escolhido
    int[] quadroDesenquadrado; // quadro depois de ser enquadrado
    // Switch para escolher a codificacao escolhida
    switch(tipoDeEnquadramento) {
      case 0: //contagem de caracteres
        quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
      case 1: //insercao de bytes
        quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBytes(quadro);
        break;
      case 2: //insercao de bits
        quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBits(quadro);
        break;
      case 3: //violacao da camada fisica
        quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramentoViolacaoCamadaFisica(quadro);
        break;
      default:
        quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres(quadro);
        break;
    } // Fim do Switch

    return quadroDesenquadrado;
  } // Fim do metodo
  /**************************************************************
  * Metodo: processarQuadro (NOVO)
  * Funcao: Verifica erros, desenquadra e passa para a proxima camada
  * @param quadro | bits recebidos (decodificados da camada fisica)
  * @return boolean | true se o quadro esta LIMPO, false se ha ERRO
  * ********************************************************* */
  public boolean processarQuadro(int[] quadro) {
    
    if (this.quadroEstaLimpo) {
        
        // 2a. Processa o quadro (Desenquadra e envia para proxima camada)
        // Usa o 'quadroVerificado' (payload) que o construtor salvou
        int[] quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramento(this.quadroVerificado);
        CamadaDeEnlaceReceptoraControleDeFluxo(quadroDesenquadrado); // (Metodo vazio)
        new CamadaDeAplicacaoReceptora(quadroDesenquadrado);
        
        // 2b. Retorna 'true' para a Camada Fisica (para enviar ACK)
        return true;
    
    } else {
        // 3. SE O QUADRO ESTIVER COM ERRO (nulo)
        System.out.println("ERRO: Quadro descartado por falha na paridade.");
        
        // 3b. Retorna 'false' para a Camada Fisica (NAO enviar ACK)
        return false;
    }
  } // Fim do metodo processarQuadro
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraControleDeErro
  * Funcao: faz o controle de erros dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraControleDeErro(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    int tipoDeControle = auxiliar.controlCodification(controller.getControleErro()); // pega o controle de erro escolhido
    int[] quadroControlado; // quadro depois de passar pelo controle de erros
    //switch para pegar o controle de erro 
    switch(tipoDeControle) {
      case 0: //bit de paridade par
        quadroControlado = CamadadeEnlaceReceptoraControleDeErroBitParidadePar(quadro);
        break;
      case 1: //bit de paridade impar
        quadroControlado = CamadadeEnlaceReceptoraControleDeErroBitParidadeImpar(quadro);
        break;
      case 2: //CRC
        quadroControlado = CamadadeEnlaceReceptoraControleDeErroCRC(quadro);
        break;
      case 3: //codigo de hamming
        quadroControlado = CamadadeEnlaceReceptoraControleDeErroCodigoDeHamming(quadro);
        break;
      default:
        quadroControlado = CamadadeEnlaceReceptoraControleDeErroBitParidadePar(quadro);
        break;
    } // Fim do switch

    return quadroControlado;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraControleDeFluxo
  * Funcao: faz o controle de fluxo dos bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  private static void CamadaDeEnlaceReceptoraControleDeFluxo(int[] quadro) {
    return;
  } // Fim do metodo
 /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres
  * Funcao: desenquadra os bits (Contagem de Caracteres) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroDesenquadrado
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    
    // Assumimos que o payload desenquadrado sera 1 int (4 bytes)
    int tamanhoFinalArray = 1;
    int[] quadroDesenquadrado = new int[tamanhoFinalArray];
    int indiceDesenquadrado = 0; // Ponteiro para a proxima posicao livre no array final

    int bitLeituraGlobal = 0; // Ponteiro para o bit que esta sendo lido
    
    // ----- INICIO DA CORRECAO -----
    // Nao usamos mais 'descobrirTotalDeBitsReais'. Um quadro enquadrado
    // pode (e deve) terminar com bits '0' de padding.
    // Usamos o tamanho fisico total do array.
    int tamanhoMaximoDeBitsNoQuadro = quadro.length * 32;
    // ----- FIM DA CORRECAO -----


    // Loop de leitura que continua ate o cursor chegar ao final do quadro
    while (bitLeituraGlobal < tamanhoMaximoDeBitsNoQuadro && indiceDesenquadrado < tamanhoFinalArray) {
      // Garante que ainda ha espaco para ler o campo de contagem (8 bits)
      if (bitLeituraGlobal + 8 > tamanhoMaximoDeBitsNoQuadro) {
        break;
      }

      int contagem = auxiliar.lerBits(quadro, bitLeituraGlobal, 8); // le os 8 bits do campo de contagem

      if (contagem == 0) { // Se o campo de contagem for 0, eh o fim dos dados.
        break;
      }

      bitLeituraGlobal += 8; // avanca o cursor apos a leitura do campo de contagem

      // calcula quantos bits a carga util tem
      // (tamanho total do frame - 1 byte do cabecalho) * 8 bits
      int bitsDaCargaUtil = (contagem - 1) * 8;
      
      if (bitsDaCargaUtil <= 0) break; // Evita erro se a contagem for 1

      // verificacao se a carga util cabe no restante do buffer
      if (bitLeituraGlobal + bitsDaCargaUtil > tamanhoMaximoDeBitsNoQuadro) {
        // A mensagem de erro agora eh mais informativa
        System.out.println("Erro: Carga util maior que o restante do quadro");
        break;
      }

      int cargaUtil = auxiliar.lerBits(quadro, bitLeituraGlobal, bitsDaCargaUtil); // le a carga util
      bitLeituraGlobal += bitsDaCargaUtil; // avanca o cursor apos a leitura da carga util

      // Adiciona a carga util lida no array de tamanho fixo
      quadroDesenquadrado[indiceDesenquadrado] = cargaUtil;
      indiceDesenquadrado++; // e avanca o ponteiro do array final
    }

    return quadroDesenquadrado; // retorna o quadro ja desenquadrado
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBytes
  * Funcao: desenquadra os bits (Insercao de Bytes) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBytes(int[] quadro) {
    final int FLAG = 0b01111110; // ~ em ASCII
    final int ESC = 0b01111101;  // } em ASCII
    
    // Desempacota o quadro de int[] para um array temporario de bytes
    int[] bytesRecebidos = new int[quadro.length * 4];
    int totalBytes = 0;
    for (int i = 0; i < quadro.length; i++) {
        int inteiroBruto = quadro[i];
        if (inteiroBruto == 0 && i > 0) break;
        bytesRecebidos[totalBytes++] = (inteiroBruto >> 24) & 0xFF;
        bytesRecebidos[totalBytes++] = (inteiroBruto >> 16) & 0xFF;
        bytesRecebidos[totalBytes++] = (inteiroBruto >> 8) & 0xFF;
        bytesRecebidos[totalBytes++] = (inteiroBruto) & 0xFF;
    }

    // Primeira Passada: Calcula o tamanho exato da carga util para criar um array do tamanho correto
    int tamanhoCargaUtil = 0;
    for (int i = 0; i < totalBytes; i++) {
        int byteAtual = bytesRecebidos[i];
        if (byteAtual == 0) continue; // Ignora bytes nulos (padding)

        // Se o byte for uma FLAG, simplesmente o ignora.
        if (byteAtual == FLAG) {
            continue;
        }

        // Se for um ESCAPE, o proximo byte eh o de dados.
        if (byteAtual == ESC) {
            i++; // Pula o byte de escape para processar o byte de dado real.
        }
        tamanhoCargaUtil++; // Conta como um byte de dado.
    }
      
    // Segunda Passada: Cria o array final e o preenche com os dados corretos
    int[] cargaUtil = new int[tamanhoCargaUtil];
    int indiceCargaUtil = 0;
    // for para percorrer tudo
    for (int i = 0; i < totalBytes && indiceCargaUtil < tamanhoCargaUtil; i++) {
        int byteAtual = bytesRecebidos[i];
        if (byteAtual == 0) continue;
        // if para verificar se eh uma flag
        if (byteAtual == FLAG) {
            continue;
        }
        // if para verificar se eh um esc
        if (byteAtual == ESC) {
            i++; // Pula o byte de escape
            if (i < totalBytes) {
                cargaUtil[indiceCargaUtil++] = bytesRecebidos[i]; // Adiciona o byte de dado
            }
        } else {
            cargaUtil[indiceCargaUtil++] = byteAtual; // Adiciona o byte de dado
        } // fim do if-else
    } // fim do for

    // Reempacota a carga util de volta para o formato int[]
    int tamanhoDesenquadrado = (tamanhoCargaUtil + 3) / 4;
    int[] quadroDesenquadrado = new int[tamanhoDesenquadrado];

    // for para primeira parte do reempacotamento
    for (int i = 0; i < tamanhoDesenquadrado; i++) {
        int inteiro = 0;
        //for para segunda parte do reempacotamento
        for (int j = 0; j < 4; j++) {
            int indice = i * 4 + j;
            // if para deslocar os bits
            if (indice < cargaUtil.length) {
                int x = cargaUtil[indice];
                inteiro |= (x << (24 - j * 8));
            } // fim do if
        } // fim do for
        quadroDesenquadrado[i] = inteiro;
    } // fim do for

    return quadroDesenquadrado;
  }
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBits
  * Funcao: desenquadra os bits (Insercao de Bits) que contem flags intermediarias
  * @param quadro | bits recebidos
  * @return quadroDesenquadrado | o quadro com os bits ja desenquadrados
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramentoInsercaoDeBits(int[] quadro) {
      FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

      // O tamanho final deve ser o do payload (1 int = 32 bits)
      int tamanhoFinalBits = 32; 
      int tamanhoFinalInts = 1; 
      int[] quadroDesenquadrado = new int[tamanhoFinalInts];
      
      int ponteiroLeitura = 8; // Pula a FLAG inicial
      int ponteiroEscrita = 0;
      int contadorDeUns = 0;
      // Aqui usamos descobrirTotalDeBitsReais pois o stuffing de bits
      // garante que o final sempre sera 01111110 (termina com 1)
      int tamanhoTotalBitsRecebidos = auxiliar.descobrirTotalDeBitsReais(quadro); 
      final int FLAG = 0b01111110;

      while(ponteiroLeitura < tamanhoTotalBitsRecebidos && ponteiroEscrita < tamanhoFinalBits) {
          // Verifica se encontrou uma FLAG (8 bits)
          if(ponteiroLeitura + 8 <= tamanhoTotalBitsRecebidos) {
              int possivelFlag = auxiliar.lerBits(quadro, ponteiroLeitura, 8);
              if (possivelFlag == FLAG) {
                  ponteiroLeitura += 8; // Pula a flag
                  contadorDeUns = 0;    // Reseta o contador
                  // Se encontramos a flag final, paramos
                  if (ponteiroEscrita > 0) break; 
                  continue;             // Volta ao inicio do loop (se for a flag inicial)
              }
          }
          
          // Se nao for uma flag, le o proximo bit
          int bit = auxiliar.lerBits(quadro, ponteiroLeitura++, 1);

          if (bit == 1) {
              contadorDeUns++;
              auxiliar.escreverBits(quadroDesenquadrado, ponteiroEscrita++, bit, 1);
          } else { // bit == 0
              if (contadorDeUns == 5) {
                  // Eh um bit de stuffing, entao o ignora
                  contadorDeUns = 0;
              } else {
                  // Eh um bit de dados normal
                  contadorDeUns = 0;
                  auxiliar.escreverBits(quadroDesenquadrado, ponteiroEscrita++, bit, 1);
              }
          }
      }

      return quadroDesenquadrado;
  } //Fim do metodo
  /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramentoViolacaoCamadaFisica
  * Funcao: desenquadra os bits (Violacao da Camada Fisica) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramentoViolacaoCamadaFisica(int[] quadro) {
    return quadro;
  } // Fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroBitParidadePar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroBitParidadePar(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController(); // Precisa do controller
    
    int totalDeBitsAConferir;
    int bitsDeDados;
    // Precisamos saber o tamanho *esperado* do quadro, pois descobrirTotalDeBitsReais()
    // falha se o bit de paridade for 0.
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        totalDeBitsAConferir = 41; // 40 bits do quadro + 1 bit de paridade
        bitsDeDados = 40;
    } else {
        // CORRECAO: Os outros metodos de enquadramento enviam 1 subquadro (32 bits) + 1 bit de paridade
        // Nao podemos usar descobrirTotalDeBitsReais() pois o bit de paridade pode ser 0.
        bitsDeDados = 32; // O payload original e sempre 32 bits
        totalDeBitsAConferir = 33; // 32 bits de dados + 1 bit de paridade
    }

    if (totalDeBitsAConferir == 0) return quadro; // Retorna o quadro vazio

    int maxBitsNoArray = quadro.length * 32;

    // Se o numero de bits que *esperamos* conferir (ex: 41)
    // for maior do que o numero de bits que *realmente existem* no array (ex: 32,
    // por causa de um erro na decodificacao), entao o quadro esta
    // irrevogavelmente corrompido. Retorna null.
    if (totalDeBitsAConferir > maxBitsNoArray) {
        System.out.println("erro na descoberta de tamannho de bits");
        return null; // Erro detectado
    }

    int contadorDeUns = 0;
    // Loop que conta TODOS os bits (dados + paridade)
    for (int i = 0; i < totalDeBitsAConferir; i++) {
        if (auxiliar.lerBits(quadro, i, 1) == 1) {
            contadorDeUns++;
        }
    }
    
    // Se a contagem total for IMPAR, o quadro esta com erro.
    if (contadorDeUns % 2 != 0) {
        return null; // Erro detectado
    }

    // Se for PAR (correto), remove o bit de paridade e retorna o payload
    int tamanhoFinalInts = (bitsDeDados + 31) / 32;
    int[] quadroSemParidade = new int[tamanhoFinalInts];

    // Copia apenas os bits de DADOS (ignora o ultimo bit)
    for (int i = 0; i < bitsDeDados; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroSemParidade, i, bit, 1);
    }

    return quadroSemParidade; // Retorna o payload limpo
  } //fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroBitParidadeImpar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroBitParidadeImpar(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController(); // Precisa do controller
    
    int totalDeBitsAConferir;
    int bitsDeDados;

    // --- INICIO DA CORRECAO ---
    if (controller.getEnquadramento().equals("Contagem de Caracteres")) {
        totalDeBitsAConferir = 41; // 40 bits do quadro + 1 bit de paridade
        bitsDeDados = 40;
    } else {
        // CORRECAO: Os outros metodos de enquadramento enviam 1 subquadro (32 bits) + 1 bit de paridade
        // Nao podemos usar descobrirTotalDeBitsReais() pois o bit de paridade pode ser 0.
        bitsDeDados = 32; // O payload original e sempre 32 bits
        totalDeBitsAConferir = 33; // 32 bits de dados + 1 bit de paridade
    }
    // --- FIM DA CORRECAO ---

    if (totalDeBitsAConferir == 0) return quadro; // Retorna o quadro vazio

    int maxBitsNoArray = quadro.length * 32;

    // Se o numero de bits que *esperamos* conferir (ex: 41)
    // for maior do que o numero de bits que *realmente existem* no array (ex: 32,
    // por causa de um erro na decodificacao), entao o quadro esta
    // irrevogavelmente corrompido. Retorna null.
    if (totalDeBitsAConferir > maxBitsNoArray) {
        System.out.println("Erro de Paridade Impar: Quadro truncado.");
        return null; // Erro detectado
    }
    int contadorDeUns = 0;
    // Loop que conta TODOS os bits (dados + paridade)
    for (int i = 0; i < totalDeBitsAConferir; i++) {
        if (auxiliar.lerBits(quadro, i, 1) == 1) {
            contadorDeUns++;
        }
    }
    
    // Se a contagem total for PAR, o quadro esta com erro.
    if (contadorDeUns % 2 == 0) {
        return null; // Erro detectado
    }

    // Se for IMPAR (correto), remove o bit de paridade e retorna o payload
    int tamanhoFinalInts = (bitsDeDados + 31) / 32;
    int[] quadroSemParidade = new int[tamanhoFinalInts];

    // Copia apenas os bits de DADOS (ignora o ultimo bit)
    for (int i = 0; i < bitsDeDados; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroSemParidade, i, bit, 1);
    }

    return quadroSemParidade; // Retorna o payload limpo
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroCRC
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroCRC(int[] quadro) {
    return quadro;
  } // fim do metodo
    /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroCodigoDeHamming
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroCodigoDeHamming(int[] quadro) {
    return quadro;
  } // fim do metodo
} // Fim da classe