/***************************************************************** * Autor............: Lucas de Menezes Chaves
* Matricula........: 202310282
* Inicio...........: 18/08/2025
* Ultima alteracao.: 29/08/2025
* Nome.............: TelaPrincipalController
* Funcao...........: Faz a mediacao entre codigo e GUI, controlando o que deve ser feito quando acontecer alguma acao na interface
*************************************************************** */
package controller;

import javafx.fxml.FXML;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import model.AplicacaoTransmissora;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.scene.control.ComboBox;
// Fim dos imports que vamos precisar

public class TelaPrincipalController {
  public static TelaPrincipalController controller; // Cria a variavel de controller para passarmos adiante no codigo

  @FXML
  private Button botaoEnviar; //Declara a variavel responsavel pelo botao

  @FXML
  private TextArea textAreaMensagemOriginal; //Declara a variavel responsavel pelo campo de texto em que o usuario digita sua mensagem

  @FXML
  private TextArea textAreaMensagemFinal; //Declara a variavel responsavel pelo campo de texto da mensagem final

  @FXML
  private TextArea textAreaCodificada; //Declara a variavel responsavel pelo campo de texto da mensagem ja codificada

  @FXML
  private TextArea textAreaDecodificada; //Declara a variavel responsavel pelo camp de texto da mensagem decodificada

  @FXML
  private ComboBox<String> comboBoxCodificacao; //Declara a variavel responsavel pelo combo box de codificacao

  @FXML
  private ComboBox<String> comboBoxEnquadramento; // Declara a variavel responsavel pelo combo box de enquadramento

  @FXML
  private ComboBox<String> comboBoxErro; // Declara a variavel responsavel pela probabilidade de erro

  @FXML
  private ComboBox<String> comboBoxControleErro; // Declara a variavel responsavel pelos algoritmos de controle de erro

  @FXML
  private Canvas canvasAnimacao; // canvas para a animacao

  private AnimationTimer animation; // animacao
  private GraphicsContext gc; // pincel da animacao

  /****************************************************************
  * Metodo: initialize
  * Funcao: carrega os elementos fxml para tela
  * @param void
  * @return void 
  * ********************************************************* */
  @FXML
  public void initialize()
  {
    controller = this; // referencia o controller para evitarmos problemas futuros usando o controller
    gc = canvasAnimacao.getGraphicsContext2D(); // pega o pincel que vamos utilizar no canvas
    comboBoxCodificacao.getItems().addAll("Binario", "Manchester", "Manchester Diferencial"); //adiciona os elementos ao combo box
    comboBoxCodificacao.getSelectionModel().selectFirst(); // Deixa o primeiro item ja selecionado
    comboBoxEnquadramento.getItems().addAll("Contagem de Caracteres", "Insercao de bytes", "Insercao de bits", "Violacao da Camada Fisica"); // adiciona os elementos ao combo box
    comboBoxEnquadramento.getSelectionModel().selectFirst(); // Deixa o primeiro item ja selecionado
    comboBoxErro.getItems().addAll("0%","10%","20%","30%","40%","50%","60%","70%","80%","90%","100%"); // adiciona os elementos ao combo box
    comboBoxErro.getSelectionModel().selectFirst(); // Deixa o primeiro item ja selecionado
    comboBoxControleErro.getItems().addAll("Bit de Paridade par", "Bit de paridade impar", "CRC", "Codigo de Hamming"); // adiciona as opcoes ao combobox
    comboBoxControleErro.getSelectionModel().selectFirst(); // deixa o primeiro item ja selecionado
  } // Fim do metodo

  /**************************************************************
  * Metodo: botao
  * Funcao: faz o botao iniciar a simulacao
  * @param void
  * @return void 
  * ********************************************************* */
  @FXML
  private void enviar()
  {
    String mensagem = textAreaMensagemOriginal.getText(); // Guarda a mensagem numa variavel tipo string

    // if para Validacao para nao aceitar mensagens vazias
    if(mensagem == null || mensagem.isEmpty())
    {
      // Verifica se a mensagem esta vazia, se estiver para o metodo
      return; //fim do metodo
    } // fim do if
    //limpa os campos de texto antes de comecar o envio de novas mensagens
    textAreaCodificada.clear();
    textAreaDecodificada.clear();
    textAreaMensagemFinal.clear();
    
    // Chamada da camada de aplicacao transmissora
    new AplicacaoTransmissora();
  } // fim do metodo

  /***********************************************************************************
  * Metodo: desempacotarBitsParaAnimacao 
  * Funcao: Pega o array de inteiros empacotados e o converte para um array simples de 0s e 1s, que a animacao consegue entender.
  * @param quadroEmpacotado O array de int[] com os bits da mensagem.
  * @param totalDeBits O numero total de bits (ex: mensagem.length() * 8).
  * @return int[] Um novo array onde cada elemento eh um unico bit (0 ou 1).
  ***********************************************************************************/
  public int[] desempacotarBitsParaAnimacao(int[] quadroEmpacotado, int totalDeBits) {
    int[] bitsParaAnimacao = new int[totalDeBits]; // cria o array para armazenar os bits que vao ser usados na animacao
    //for para coletar os bits
    for (int i = 0; i < totalDeBits; i++) {
        bitsParaAnimacao[i] = getBitAtIndex(quadroEmpacotado, i);
    } // fim do for
    return bitsParaAnimacao; // retorno da funcao
  } // fim do metodo

/***********************************************************************************
  * Metodo: getBitAtIndex
  * Funcao: Pega um unico bit de um array de inteiros "empacotados".
  * @param dadosEmpacotados O array de int[] com os bits.
  * @param indiceGeralDoBit A posicao do bit na mensagem inteira.
  * @return int | 0 ou 1 (valor do bit).
  ***********************************************************************************/
  private int getBitAtIndex(int[] dadosEmpacotados, int indiceGeralDoBit) {
    // Descobre em qual 'int' do array o nosso bit esta.
    int indiceDoInt = indiceGeralDoBit / 32;

    // Verificacao de seguranca para evitar que o programa quebre
    if (indiceDoInt >= dadosEmpacotados.length) {
      return 0; // retorna 0
    }
    
    // Pega o inteiro que contem o bit desejado.
    int numeroEmpacotado = dadosEmpacotados[indiceDoInt];
    
    // Descobre a posicao exata do bit dentro desse inteiro.
    int indiceDoBitNoInt = 31 - (indiceGeralDoBit % 32);
    
    // Isola o bit especifico usando operadores bitwise e o retorna.
    return (numeroEmpacotado >> indiceDoBitNoInt) & 1;
  } // fim do metodo

  /**************************************************************
  * Metodo: desenharSinalTransmissao
  * Funcao: realiza a animacao da onda quadrada para simular a transmissao de bits
  * @param bits | bits a serem transmitidos (um bit por posicao do array)
  * @return void
  * ********************************************************* */
  public void drawSignal(int[] bits) {
    // Finaliza possiveis animacoes anteriores antes de comecar
    // if para verificar se a animacao eh nula
    if (animation != null) {
      animation.stop(); // para a animacao
    } // fim do if
    // if para verificar se os bits sao nulos
    if (bits == null || bits.length == 0) {
        return; // fim precoce da funcao
    } // fim do if

    final double LARGURA_BIT;
    // if para verificar a codificacao
    if (getCodificacao().equals("Manchester") || getCodificacao().equals("Manchester Diferencial")) {
      LARGURA_BIT = 20.0;
    } else {
      LARGURA_BIT = 40.0;
    } // fim do if-else

    final double ALTURA_GRAFICO = canvasAnimacao.getHeight();
    final double NIVEL_ALTO_Y = ALTURA_GRAFICO * 0.25;
    final double NIVEL_BAIXO_Y = ALTURA_GRAFICO * 0.75;
    final double VELOCIDADE_PX_POR_SEGUNDO = 80.0;
    final double LARGURA_TOTAL_DA_ONDA = bits.length * LARGURA_BIT;
    final long tempoInicialNano = System.nanoTime();

    animation = new AnimationTimer() {
      /**************************************************************
      * Metodo: handle
      * Funcao: realiza a parte de calculos e verificacao da animacao
      * @param now | temporizador
      * @return void
      * ********************************************************* */
      @Override
      public void handle(long now) {
        double tempoDecorridoSeg = (now - tempoInicialNano) / 1_000_000_000.0;
        double offsetX = tempoDecorridoSeg * VELOCIDADE_PX_POR_SEGUNDO;
        double posicaoInicialDaOnda = offsetX - LARGURA_TOTAL_DA_ONDA;

        gc.clearRect(0, 0, canvasAnimacao.getWidth(), ALTURA_GRAFICO);
        gc.setStroke(Color.web("#00FF00"));
        gc.setLineWidth(2.5);

        // Assumimos que o sinal comeca em "baixo" antes do primeiro bit
        double pontoYAnterior = NIVEL_BAIXO_Y; 

        // for para realizar as animacoes
        for (int i = 0; i < bits.length; i++) {
          double startX = posicaoInicialDaOnda + (i * LARGURA_BIT);
          double endX = startX + LARGURA_BIT;
          double pontoYAtual = (bits[i] == 1) ? NIVEL_ALTO_Y : NIVEL_BAIXO_Y;

          // if para verificar as posicoes de animacao
          if (endX < 0 || startX > canvasAnimacao.getWidth()) {
             pontoYAnterior = pontoYAtual;
             continue;
          } // fim do if

          // if para verificar os pontos de animacao
          if (pontoYAtual != pontoYAnterior) {
            gc.strokeLine(startX, pontoYAnterior, startX, pontoYAtual);
          } // fim do if

          gc.strokeLine(startX, pontoYAtual, endX, pontoYAtual);
          pontoYAnterior = pontoYAtual;
        }

        // if para verificar o inicio da animacao
        if (posicaoInicialDaOnda > canvasAnimacao.getWidth()) {
          this.stop();
          gc.clearRect(0, 0, canvasAnimacao.getWidth(), ALTURA_GRAFICO);
        } // fim do if
      } // fim do metodo
    };
    animation.start(); // comeca a animacao
  } // fim do metodo

  /****************************************************************
  * Metodo: setTextAreaCodificada
  * Funcao: muda o texto do campo de texto de mensagem codificada
  * @param texto | o texto que vai aparecer na caixa de texto
  * @return void 
  * ********************************************************* */
  public void setTextAreaCodificada(String texto)
  {
    textAreaCodificada.setText(texto); // define o texto
  } // Fim do metodo

  /****************************************************************
  * Metodo: setTextAreaDecodificada
  * Funcao: muda o texto do campo de texto da mensagem decodificada
  * @param texto | o texto que vai aparecer na caixa de texto
  * @return void 
  * ********************************************************* */
  public void setTextAreaDecodificada(String texto)
  {
    textAreaDecodificada.setText(texto); // define o texto
  } // Fim do metodo

  /****************************************************************
  * Metodo: setTextAreaMensagemFinal
  * Funcao: muda o texto do text area da mensagem final
  * @param texto | o texto que vai aparecer na caixa de texto
  * @return void 
  * ********************************************************* */
  public void setTextAreaMensagemFinal(String texto)
  {
    textAreaMensagemFinal.setText(texto); // define o texto
  } // Fim do metodo

  /****************************************************************
  * Metodo: getTextFieldCodificada
  * Funcao: retorna o texto da caixa de texto da mensagem codificada
  * @param void
  * @return String | texto que esta na caixa de texto codificada
  * ********************************************************* */
  public String getTextFieldCodificada()
  {
    return textAreaCodificada.getText(); // retorno da funcao
  } // Fim do metodo

  /****************************************************************
  * Metodo: getCodificacao
  * Funcao: retorna o valor do combo box para escolher a codificacao
  * @param void
  * @return String | o metodo de codificacao escolhido
  * ********************************************************* */
  public String getCodificacao()
  {
    return comboBoxCodificacao.getValue(); // retorno da funcao
  } // Fim do metodo

  /****************************************************************
  * Metodo: getControleErro
  * Funcao: retorna o valor do combo box para escolher o algoritmo de controle de erro
  * @param void
  * @return String | o metodo de codificacao escolhido
  * ********************************************************* */
    public String getControleErro()
  {
    return comboBoxControleErro.getValue(); // retorno da funcao
  } // Fim do metodo
  /****************************************************************
  * Metodo: getEnquadramento
  * Funcao: retorna o valor do combo box para escolher o enquadramento
  * @param void
  * @return String | o metodo de codificacao escolhido
  * ********************************************************* */
    public String getEnquadramento()
  {
    return comboBoxEnquadramento.getValue(); // retorno da funcao
  } // Fim do metodo

  /****************************************************************
  * Metodo: getMensagemOriginal
  * Funcao: retorna o texto contido no campo de texto da mensagem original
  * @param void
  * @return String | texto que esta na caixa de texto da mensagem original
  * ********************************************************* */
  public String getMensagemOriginal()
  {
    return textAreaMensagemOriginal.getText(); // retorno da funcao
  } // Fim do metodo
  /****************************************************************
  * Metodo: getController
  * Funcao: retorna o controller
  * @param void
  * @return controller | controller para fazermos alteracoes na GUI
  * ********************************************************* */
  public static TelaPrincipalController getController() {
    return controller; // retorno da funcao
  } // fim do metodo
  /****************************************************************
  * Metodo: getTaxaDeErro
  * Funcao: Pega o valor em String da ComboBox (ex: "10%"), converte
  * para um double (ex: 0.1) e o retorna.
  * @param void
  * @return double | a taxa de erro para calculo de probabilidade
  * ********************************************************* */
  public double getTaxaDeErro() {
    String valorSelecionado = comboBoxErro.getValue();
    
    if (valorSelecionado == null || valorSelecionado.isEmpty()) {
      return 0.0; // Retorna 0 se nada for selecionado
    }
    
    try {
      // Remove o caractere '%' e converte o resto da string para um numero
      String numeroString = valorSelecionado.replace("%", "").trim();
      int valorInteiro = Integer.parseInt(numeroString);
      return valorInteiro / 100.0; // Converte a porcentagem para um valor double (10 -> 0.1)
    } catch (NumberFormatException e) {
      e.printStackTrace();
      return 0.0; // Retorna 0 em caso de erro na conversao
    }
  } // fim do metodo

  /****************************************************************
  * Metodo: getTextAreaMensagemFinal
  * Funcao: retorna o texto contido no campo de texto da mensagem final
  * (Necessario para acumulacao concorrente)
  * @param void
  * @return String | texto que esta na caixa de texto da mensagem final
  * ********************************************************* */
  public String getTextAreaMensagemFinal()
  {
    return textAreaMensagemFinal.getText(); // retorno da funcao
  } // Fim do metodo
} // fim da classe