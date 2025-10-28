/***************************************************************** 
* Autor..............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 16/09/2025
* Ultima alteracao.: 27/09/2025
* Nome.............: CamadaEnlaceDadosReceptora
* Funcao...........: Transfere a mensagem decodificada e desenquadrada para camada aplicacao receptora
*************************************************************** */
package model;

import controller.TelaPrincipalController;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import utils.FuncoesAuxiliares;

public class CamadaEnlaceDadosReceptora {
  /**************************************************************
  * Metodo: CamadaEnlaceDadosReceptora
  * Funcao: desenquadra os bits e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return void 
  * ********************************************************* */
  public CamadaEnlaceDadosReceptora(int[] quadro) {
    int[] quadroOrdenado = CamadaDeEnlaceReceptoraControleDeFluxo(quadro);
    int[] quadroControlado = CamadaDeEnlaceReceptoraControleDeErro(quadroOrdenado);
    int[] quadroDesenquadrado = CamadaDeEnlaceReceptoraEnquadramento(quadroControlado);

    new CamadaDeAplicacaoReceptora(quadroDesenquadrado);
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
  private static int[] CamadaDeEnlaceReceptoraControleDeFluxo(int[] quadro) {
    return quadro;
  } // Fim do metodo
 /**************************************************************
  * Metodo: CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres
  * Funcao: desenquadra os bits (Contagem de Caracteres) e passa eles para camada seguinte
  * @param quadro | bits recebidos
  * @return quadroDesenquadrado
  * ********************************************************* */
  private static int[] CamadaDeEnlaceReceptoraEnquadramentoContagemDeCaracteres(int[] quadro) {
FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    TelaPrincipalController controller = TelaPrincipalController.getController();
    String mensagemOriginal = controller.getMensagemOriginal();

    // Calcula o tamanho exato que o array final precisa ter
    int tamanhoFinalArray = (mensagemOriginal.length() + 3) / 4;
    int[] quadroDesenquadrado = new int[tamanhoFinalArray];
    int indiceDesenquadrado = 0; // Ponteiro para a proxima posicao livre no array final

    int bitLeituraGlobal = 0; // Ponteiro para o bit que esta sendo lido
    int tamanhoMaximoDeBitsNoQuadro = quadro.length * 32;

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

      // verificacao se a carga util cabe no restante do buffer
      if (bitLeituraGlobal + bitsDaCargaUtil > tamanhoMaximoDeBitsNoQuadro) {
        System.out.println("Erro: Carga util maior que o restante do quadro.");
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
      TelaPrincipalController controller = TelaPrincipalController.getController();

      // O tamanho final deve ser exatamente o da mensagem original
      int tamanhoFinalBits = controller.getMensagemOriginal().length() * 8;
      int tamanhoFinalInts = (tamanhoFinalBits + 31) / 32;
      int[] quadroDesenquadrado = new int[tamanhoFinalInts];
      int ponteiroLeitura = 8; // Pula a FLAG inicial
      int ponteiroEscrita = 0;
      int contadorDeUns = 0;
      int tamanhoTotalBitsRecebidos = quadro.length * 32;
      final int FLAG = 0b01111110;

      while(ponteiroLeitura < tamanhoTotalBitsRecebidos && ponteiroEscrita < tamanhoFinalBits) {
          // Verifica se encontrou uma FLAG (8 bits)
          if(ponteiroLeitura + 8 <= tamanhoTotalBitsRecebidos) {
              int possivelFlag = auxiliar.lerBits(quadro, ponteiroLeitura, 8);
              if (possivelFlag == FLAG) {
                  ponteiroLeitura += 8; // Pula a flag
                  contadorDeUns = 0;    // Reseta o contador para o proximo sub-quadro
                  continue;             // Volta ao inicio do loop
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

    // Descobre o tamanho total de bits, incluindo o bit de paridade
    int totalBitsRecebidos = quadro.length * 32;

    // Se o quadro estiver vazio, nao ha nada a fazer.
    if (totalBitsRecebidos == 0) {
      return quadro;
    }

    // 2. inicializar contador de um
    int contadorDeUns = 0;

    // 3. percorrer quadro recebido[]
    // 4. para cada 1 em quadro recebido[]
    for (int i = 0; i < totalBitsRecebidos; i++) {
      if (auxiliar.lerBits(quadro, i, 1) == 1) {
        contadorDeUns++; // 5. contador de um ++
      }
    } // 6. fim do para cada

    System.out.println(contadorDeUns);

    // 7. se contador de um % 2 != 0 (impar, indica erro)
    if (contadorDeUns % 2 != 0) {
      // 9. alerta(houve um erro de paridade)
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Erro de Transmissão");
      alert.setHeaderText("Erro de Paridade Detectado");
      alert.setContentText("Um erro foi detectado nos dados recebidos! O controle de paridade par falhou (a contagem de bits '1' é ímpar).");
      
      // Eh melhor usar show() se a simulacao precisar continuar rodando
      alert.show(); 
    }
    // 8. nao houve erro (Nao faz nada, conforme solicitado)
    // 10. fim do se

    // Agora, removemos o bit de paridade para passar apenas os dados
    
    // 1. inicializar quadro controlado
    // O quadro de dados real eh 1 bit menor que o quadro recebido
    int totalBitsDeDados = totalBitsRecebidos - 1;

    // Se o quadro so tinha o bit de paridade (ou estava vazio), retorna vazio
    if (totalBitsDeDados <= 0) {
        return new int[0];
    }

    // Calcula o tamanho do novo array de int[]
    int tamanhoNovoArray = (totalBitsDeDados + 31) / 32;
    int[] quadroControlado = new int[tamanhoNovoArray];

    // 11. remover quadrorecebido[ultima posicao]
    // 12. quadro controlado = quadro recebido
    // (Isso eh feito copiando todos os bits, *exceto* o ultimo)
    for (int i = 0; i < totalBitsDeDados; i++) {
      int bit = auxiliar.lerBits(quadro, i, 1);
      auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    // 13. retorne quadro controlado
    return quadroControlado;
  } //fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroBitParidadeImpar
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroBitParidadeImpar(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();

    // Descobre o tamanho total de bits, incluindo o bit de paridade
    int totalBitsRecebidos = quadro.length * 32;

    // Se o quadro estiver vazio, nao ha nada a fazer.
    if (totalBitsRecebidos == 0) {
      return quadro;
    }

    // 2. inicializar contador de um
    int contadorDeUns = 0;

    // 3. percorrer quadro recebido[]
    // 4. para cada 1 em quadro recebido[]
    for (int i = 0; i < totalBitsRecebidos; i++) {
      if (auxiliar.lerBits(quadro, i, 1) == 1) {
        contadorDeUns++; // 5. contador de um ++
      }
    } // 6. fim do para cada

    System.out.println(contadorDeUns);

    // 7. se contador de um % 2 == 0 (par, indica erro)
    if (contadorDeUns % 2 == 0) {
      // 9. alerta(houve um erro de paridade)
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Erro de Transmissão");
      alert.setHeaderText("Erro de Paridade Detectado");
      alert.setContentText("Um erro foi detectado nos dados recebidos! O controle de paridade impar falhou (a contagem de bits '1' é par).");
      
      // showAndWait() trava a execucao ate o usuario fechar o alerta
      // Eh melhor usar show() se a simulacao precisar continuar rodando
      alert.show(); 
    }
    // 8. nao houve erro (Nao faz nada, conforme solicitado)
    // 10. fim do se

    // Agora, removemos o bit de paridade para passar apenas os dados
    
    // 1. inicializar quadro controlado
    // O quadro de dados real eh 1 bit menor que o quadro recebido
    int totalBitsDeDados = totalBitsRecebidos - 1;

    // Se o quadro so tinha o bit de paridade (ou estava vazio), retorna vazio
    if (totalBitsDeDados <= 0) {
        return new int[0];
    }

    // Calcula o tamanho do novo array de int[]
    int tamanhoNovoArray = (totalBitsDeDados + 31) / 32;
    int[] quadroControlado = new int[tamanhoNovoArray];

    // 11. remover quadrorecebido[ultima posicao]
    // 12. quadro controlado = quadro recebido
    // (Isso eh feito copiando todos os bits, *exceto* o ultimo)
    for (int i = 0; i < totalBitsDeDados; i++) {
      int bit = auxiliar.lerBits(quadro, i, 1);
      auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    // 13. retorne quadro controlado
    return quadroControlado;
  } // fim do metodo
  /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroCRC
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroCRC(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    
    // Descobre o tamanho total de bits, incluindo o bit de paridade
    int totalBitsRecebidos = quadro.length * 32;

    if (totalBitsRecebidos < 32) {
        // Frame muito curto para conter CRC, considera erro ou frame vazio
        if(totalBitsRecebidos > 0) {
          Alert alert = new Alert(AlertType.ERROR);
          alert.setTitle("Erro de Transmissão");
          alert.setHeaderText("Erro de CRC Detectado");
          alert.setContentText("Quadro recebido é menor que o proprio CRC.");
          alert.show(); 
        }
        return new int[0]; // Retorna vazio
    }

    int totalBitsDeDados = totalBitsRecebidos - 32;

    // 1. Calcular o CRC com base APENAS nos dados recebidos
    final int POLY = 0x04C11DB7;
    final int INIT = 0xFFFFFFFF;
    final int XOROUT = 0xFFFFFFFF;
    int reg = INIT;

    // Processar bits de dados (excluindo o CRC anexado)
    for (int i = 0; i < totalBitsDeDados; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1); 
        int top_bit = (reg >>> 31) & 1;
        reg = (reg << 1) | bit;
        
        if (top_bit == 1) {
            reg = reg ^ POLY;
        }
    }

    // Processar os 32 bits '0' virtuais
    for (int i = 0; i < 32; i++) {
        int top_bit = (reg >>> 31) & 1;
        reg = (reg << 1) | 0;
        
        if (top_bit == 1) {
            reg = reg ^ POLY;
        }
    }

    int crcCalculado = reg ^ XOROUT;

    // 2. Ler o CRC que foi anexado no final do quadro
    int crcRecebido = auxiliar.lerBits(quadro, totalBitsDeDados, 32);

    if (crcCalculado != crcRecebido) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Erro de Transmissão");
        alert.setHeaderText("Erro de CRC Detectado");
        
        // Correcao para StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("Um erro foi detectado nos dados recebidos! O CRC falhou.\n");
        sb.append("Calculado: 0x").append(Integer.toHexString(crcCalculado).toUpperCase()).append("\n");
        sb.append("Recebido:  0x").append(Integer.toHexString(crcRecebido).toUpperCase());
        
        alert.setContentText(sb.toString());
        alert.show(); 
    }

    // 4. Remover o CRC e retornar apenas os dados
    if (totalBitsDeDados == 0) {
        return new int[0]; // Nao havia dados, apenas CRC
    }

    int tamanhoNovoArray = (totalBitsDeDados + 31) / 32;
    int[] quadroControlado = new int[tamanhoNovoArray];

    // Copia todos os bits, *exceto* os ultimos 32
    for (int i = 0; i < totalBitsDeDados; i++) {
        int bit = auxiliar.lerBits(quadro, i, 1);
        auxiliar.escreverBits(quadroControlado, i, bit, 1);
    }

    return quadroControlado;
  } // fim do metodo
    /**************************************************************
  * Metodo: CamadadeEnlaceReceptoraControleDeErroCodigoDeHamming
  * Funcao: adiciona cargas de controle aos bits para ficarem aparentemente livres de erro e passam para proxima camada
  * @param quadro | bits recebidos
  * @return quadro 
  * ********************************************************* */
  private static int[] CamadadeEnlaceReceptoraControleDeErroCodigoDeHamming(int[] quadro) {
    FuncoesAuxiliares auxiliar = new FuncoesAuxiliares();
    int N = quadro.length * 32; // N = tamanho total do quadro recebido

    if (N == 0) {
      return new int[0];
    }

    // 1. Calcular a Sindrome (baseado no pseudocodigo)
    int syndrome = 0;
    int posParidade = 1;

    // 'r' eh o numero de bits de paridade, precisamos conta-los
    int r = 0; 
    
    // WHILE pos_paridade <= N
    while (posParidade <= N) {
      r++; // Conta quantos bits de paridade existem
      int soma = 0;

      // FOR i FROM 1 TO N
      for (int i = 1; i <= N; i++) {
        // IF (i AND pos_paridade) != 0
        if ((i & posParidade) != 0) {
          int bit = auxiliar.lerBits(quadro, i - 1, 1); // Le da posicao 0-based
          soma = soma ^ bit; // soma = soma XOR received_bits[i]
        }
      }

      // IF soma != 0
      if (soma != 0) {
        syndrome = syndrome + posParidade; // Marca a paridade que falhou
      }
      
      posParidade = posParidade * 2; // Proximo bit de paridade
    }

    // 3) Analisar a sindrome
    if (syndrome != 0) {
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("Erro de Transmissão");
      alert.setHeaderText("Erro de Hamming Detectado!");

      StringBuilder sb = new StringBuilder();
      sb.append("Um erro foi detectado nos dados recebidos!\n");
      sb.append("A verificação de Hamming falhou.\n");
      sb.append("Posição do erro (Síndrome): ").append(syndrome);
      
      alert.setContentText(sb.toString());
      alert.show();
      // NOTA: O exercicio nao pede correcao, apenas deteccao.
      // Se pedisse, poderiamos inverter o bit na 'posicao - 1' (syndrome - 1)
    }

    // 4. Extrair os bits de dados originais (remover os bits de paridade)
    int totalDataBits = N - r;
    if (totalDataBits <= 0) {
      return new int[0];
    }

    int tamanhoNovoArray = (totalDataBits + 31) / 32;
    int[] quadroControlado = new int[tamanhoNovoArray];
    int ponteiroEscrita = 0;

    // Itera por todas as posicoes do quadro recebido
    for (int pos = 1; pos <= N; pos++) {
      // Se NAO for potencia de 2, eh um bit de dado
      if ((pos & (pos - 1)) != 0) {
        if (ponteiroEscrita < totalDataBits) {
          int bit = auxiliar.lerBits(quadro, pos - 1, 1);
          auxiliar.escreverBits(quadroControlado, ponteiroEscrita, bit, 1);
          ponteiroEscrita++;
        }
      }
    }

    return quadroControlado;
  } // fim do metodo
} // Fim da classe