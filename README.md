# JMGroupTool
Ferramenta para agilizar e facilitar a criação de gráficos com os resultados gerados pelo JMeter.

## Contexto
O JMGroupTool veio da necessidade de agilizar e facilitar a criação de alguns gráficos personalizados para serem apresentados no relatório de performance. Esses gráficos são:
  * Tempo médio por grupo de usuários virtuais (v.u.)
  * Taxa de erro por grupo de v.u.
  * Tempo médio por transação (com granularidade personalizada)
  * Tempo médio por transação sobreposto com a quantidade de v.u.
  
### Tempo médio por grupo de usuários simultâneos
A idéia desse gráfico é demonstrar de forma direta o impacto causado pela entrada de usuários no tempo médio no decorrer da execução. Nesse gráfico o tempo médio é calculado tendo em vista o ramp up da execução. Por exemplo, num ramp up com três etapas: 10, 20 e 30 usuários simultâneos teremos um gráfico com 3 pontos correspondentes.

![Tempo médio por grupo de v.u.](https://i.imgur.com/gZU6O4M.png)

Nesse gráfico é considerado apenas as transações em que a quantidade de usuários virtuais é exata ao especificado, assim a escalada entre a subida de usuários e o ramp down não são considerados.

### Taxa de erro por grupo de usuários simultâneos
Esse gráfico, de forma similar ao gráfico acima, tem como objetivo demonstrar de forma direta o impacto causado pela entrada de usuários na taxa de erro no decorrer da execução.

![Taxa de erro por grupo de usuários virtuais](https://i.imgur.com/tDw7yZe.png)

Da mesma forma, nesse gráfico é considerado apenas as transações em que quantidade de usuários virtuais é exata ao especificado, assim a escalada entre a a subida de usuários e o ramp down não são considerados.

### Tempo médio por transação (com granularidade personalizada)
Aqui o gráfico exibe o tempo médio no decorrer de toda a execução. O JMGroupTool permite que, pós-execução, seja definido uma granularidade personalizada a fim de gerar um gráfico que mais agrade o usuário.

![Tempo de resposta(s) no decorrer da execução](https://i.imgur.com/X9NgsmV.png)

O gráfico acima foi feito com uma granularidade de 60 segundo. Tendo os dados da execução o JMGroupTool permite ajustar a granularidade a fim de gerar mais ou menos pontos no gráfico final.

### V. U. sobrepondo tempo médio

O gráfico abaixo relaciona o tempo médio com a quantidade de v.u., isso permite observar o impacto da entrada de usuários no decorrer da execução.

![Tempo médio(s) x V.U.](https://i.imgur.com/4CVpQll.png)

## Pré-requisitos

- O JMGroupTool requer um arquivo (.txt, .csv ou .xml) gerado pelo JMeter. Esse arquivo pode ser gerado de duas formas, via execução pelo console ou pelo componente Simple Data Writer.
- Java 8
- Maven
- Microsoft Excel ou similar para gerar os gráficos com os dados retornados pelo JMGroupTool

### Gerando dados via console

Via execução no console deve-se definir o diretório para o .csv com o log das chamadas realizada, via atributo '`-l`':

`jmeter -n -t <caminho - Localização do script .jmx> -l <caminho - Localização dos resultados em tabela .csv> -e -o <caminho - Localização dos relatórios HTML>`

**Não alterar o arquivo de log gerado pelo JMeter. Edições no mesmo podem causar incompatibilidade ao fazer a leitura no JMGroupTool.**

Caso você tenha definido um formato de data personalizado para time stamps no JMeter via arquivo de propriedades ([JMETER_HOME]/bin/jmeter.properties propriedade jmeter.save.saveservice.timestamp_format) você deve definir o mesmo valor no arquivo config.properties na pasta raiz do JMGroupTool. O valor padrão é 'ms'.

### Gerando dados via Simple Data Writer

Adicione o componente do tipo listener Simple Data Writer. No componente deixe **desmarcado** as opções:
- Log/Display Only: Errors
- Log/Display Only: Successes

Nas opções exibidas através do botão Configure são obrigatórias as opções:
- Save as XML
- Save Elapsed Time
- Save Success
- Save Active Threads Counts
- Save Time Stamp

Ainda nas opções do botão Configure é altamente recomendado deixar marcado a opção 'Save Labels' para ser possível filtrar por diferentes tipos de requisição.

As demais configurações não são necessárias/utilizadas no JMGroupTool mas podem ser de interesse do usuário para obter dados relevantes da execução dado a necessidade e o contexto da mesma. Mas para as opções a seguir o arquivo de log vai ocupar mais espaço em disco além de ser mais custoso em processamento, assim marcar elas deve-se ser feito com mais cautela:
- Save Response Data (XML)
- Save Request Headers (XML)
- Save Sampler Data (XML)
- Save Response Header (XML)

Exemplo de configuração do Simple Data Writer:

![Exemplo de Simple Data Writer configuração do Simple Data Writer](https://i.imgur.com/WgOBwG1.png)

Um exemplo de projeto JMeter está na pasta exemplo-projeto-jmeter. É necessário ter o plugin Ultimate Thread Group instalado no JMeter.

**Não alterar o arquivo de log gerado pelo JMeter. Edições no mesmo podem causar incompatibilidade ao fazer a leitura no JMGroupTool.**

## Utilização

### Tempo médio e taxa de erros por grupos  de v.u.

1. Importe o arquivo gerado pela execução do JMeter conforme detalhado na seção [pré-requisitos](https://github.com/carloscamposiki/JMGroupTool/blob/master/README.md#pr%C3%A9-requisitos)
2. Selecione a label desejada (são as mesmas labels do projeto do JMeter)
![Selecione a label](https://i.imgur.com/TRIyJxv.png)
3. Defina se quer ignorar ou não o ramp down (por padrão ativado)
![Ignore ramp down](https://i.imgur.com/oCt81H3.png)
4. Escolha os grupos de v.u. para gerar os dados. A quantidade de transações já indica quais grupos são interessantes, você  também pode consultar o ramp up definido no JMeter.
![Defina os grupos de v.u.](https://i.imgur.com/WNTI2Ev.png)
5. Clique em Processar, copie os dados retornados para o Excel ou ferramenta e construa os seus gráficos([exemplo tempo médio](https://github.com/carloscamposiki/JMGroupTool#tempo-m%C3%A9dio-por-grupo-de-usu%C3%A1rios-simult%C3%A2neos) e [exemplo taxa de erro](https://github.com/carloscamposiki/JMGroupTool#taxa-de-erro-por-grupo-de-usu%C3%A1rios-simult%C3%A2neos))

### Tempo médio no decorrer da execução sobrepondo com a quantidade de v.u.
1. Importe o arquivo gerado pela execução do JMeter conforme detalhado na seção [pré-requisitos](https://github.com/carloscamposiki/JMGroupTool/blob/master/README.md#pr%C3%A9-requisitos)
2. Selecione a label desejada (são as mesmas labels do projeto do JMeter)
![Selecione a label](https://i.imgur.com/TRIyJxv.png)
3. Defina se você quer ou não que seja retornado também a quantidade de v.u. para gerar um gráfico sobreposto (por padrão ativado)
![Retornar quantidade de v.u.](https://i.imgur.com/ob5AonB.png)
4. Clique em Processar, copie os dados retornados para o Excel ou ferramenta e construa o seu gráfico([exemplo](https://github.com/carloscamposiki/JMGroupTool#v-u-sobrepondo-tempo-m%C3%A9dio))

## Contato
Se quiser reportar problemas ou fazer sugestões envie um email para [carloscamposhs@gmail.com](mailto:carloscamposhs@gmail.com) ou mande um DM pelo Twitter [@camposiki](http://www.twitter.com/camposiki).
