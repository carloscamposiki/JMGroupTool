package com.carlos.panes;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.carlos.core.FileParser;
import com.carlos.core.ProcessData;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class MainPane {
	private Pane pane;
	private String fileName;
	private File file;
	private Stage stage;
	private FileParser fileParser = new FileParser();
	private JSONArray data = null;
	private JSONArray filteredData = null;
	private boolean hasLabels = false;
	private String selectedLabel = "";
	private boolean ignoreRampdownFlag = true;
	private ScrollPane groupsSC = new ScrollPane();
	private Button process;
	private List<Integer> selectedGroups = new ArrayList<Integer>();
	private boolean group = true;
	private Pattern numberPattern = Pattern.compile("[0-9]*");
	private TextField granularity;
	private CheckBox showVU;
	private int graV = 0;
	
	public MainPane(Stage stage) {
		this.stage = stage;
		pane = new Pane();
		pane.setMaxSize(1100, 640);
		pane.setMinSize(1100, 640);
		drawHomePane();
	}

	private void drawFileLoadedPane() {
		
		ScrollPane labelsSC;

		pane.getChildren().clear();

		Label title = new Label("JMGroupTool");
		title.setLayoutX(40);
		title.setLayoutY(10);
		title.setStyle("-fx-font-size: 20px;  -fx-font-weight: bold;");

		Button about = new Button("SOBRE");
		about.setLayoutX(1000);
		about.setLayoutY(12);
		about.setStyle("-fx-font-size: 14px;");
		about.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				    try {
						Desktop.getDesktop().browse(new URI("https://github.com/carloscamposiki/JMGroupTool#jmgrouptool"));
					} catch (IOException | URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		Label subTitle = new Label("Configure a extração de dados para o arquivo selecionado("
				+ (fileName.length() > 200 ? fileName.substring(0, 200) + "[...]" : fileName) + ")");
		subTitle.setLayoutX(40);
		subTitle.setLayoutY(50);
		subTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

		Label labelTitle = new Label("Selecione a label desejada:");
		labelTitle.setLayoutX(40);
		labelTitle.setLayoutY(87);
		labelTitle.setStyle("-fx-font-size: 14px;");

		labelsSC = new ScrollPane();
		labelsSC.setLayoutX(40);
		labelsSC.setLayoutY(120);
		labelsSC.setMinSize(1021, 100);
		labelsSC.setMaxSize(1021, 100);
		labelsSC.setHbarPolicy(ScrollBarPolicy.NEVER);
		drawLabels(labelsSC, null);
		
		TextField filterLabel = new TextField();
		filterLabel.setPromptText("Filtrar labels...");
		filterLabel.setLayoutX(861);
		filterLabel.setLayoutY(84);
		filterLabel.setMinWidth(200);
		filterLabel.setMaxWidth(200);
		filterLabel.setStyle("-fx-font-size: 14px;");
		filterLabel.textProperty().addListener((observable, oldValue, newValue) -> {
			if(oldValue.equals(newValue)) return;
			drawLabels(labelsSC, newValue);
			textSize(newValue);
		});
		
		if(group) {
			CheckBox ignoreRampdown = new CheckBox("Ignorar rampdown");
			ignoreRampdown.setLayoutX(40);
			ignoreRampdown.setLayoutY(237);
			ignoreRampdown.setStyle("-fx-font-size: 14px;");
			ignoreRampdown.setSelected(true);

			Label groupsLabel = new Label("Selecione os grupos por quantidade de usuários simultâneos:");
			groupsLabel.setLayoutX(40);
			groupsLabel.setLayoutY(270);
			groupsLabel.setStyle("-fx-font-size: 14px;");

			groupsSC.setLayoutX(40);
			groupsSC.setLayoutY(300);
			groupsSC.setMinSize(1021, 100);
			groupsSC.setMaxSize(1021, 100);
			groupsSC.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			ignoreRampdown.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent arg0) {
					setIgnoreRampdown(ignoreRampdown.isSelected());
					drawGroups();
				}
			});
			
			pane.getChildren().addAll(ignoreRampdown, groupsLabel, groupsSC);
		} else {
			showVU = new CheckBox("Relacionar o tempo médio com a quantidade de v.u.");
			showVU.setLayoutX(40);
			showVU.setLayoutY(237);
			showVU.setStyle("-fx-font-size: 14px;");
			showVU.setSelected(true);
			
			Label granularityLabel = new Label("Defina a granularidade em segundos:");
			granularityLabel.setLayoutX(40);
			granularityLabel.setLayoutY(270);
			granularityLabel.setStyle("-fx-font-size: 14px;");
			
			Label minGran = new Label("Granularidade deve ser maior ou igual 60 segundos.");
			minGran.setLayoutX(40);
			minGran.setLayoutY(330);
			minGran.setStyle("-fx-text-fill: red;");
			minGran.setVisible(false);
			
			granularity = new TextField();
			granularity.setLayoutX(40);
			granularity.setLayoutY(300);
			granularity.setStyle("-fx-font-size: 14px;");
			granularity.setMinWidth(260);
			granularity.textProperty().addListener((observable, oldValue, newValue) -> {
			    Matcher matcher = numberPattern.matcher(newValue);
			    if(!matcher.matches()) {
			    	granularity.setText(oldValue);
			    }
			    if(newValue.length()>0) {
			    	graV = Integer.parseInt(newValue);
			    } else {
			    	graV = 0;
			    }
			    minGran.setVisible(graV<60);
			    process.setDisable((selectedLabel.length()==0 && hasLabels) || graV<60);
			});
			
			pane.getChildren().addAll(showVU, granularityLabel, granularity, minGran);
		}
		

		process = new Button("PROCESSAR DADOS");
		process.setLayoutX(475);
		process.setLayoutY(group ? 460 : 400);
		process.setMinWidth(150);
		process.setStyle("-fx-font-size: 14px;");
		process.setDisable(true);
		process.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					if(group) {						
						new ProcessData().processGroups(ignoreRampdownFlag, filteredData, selectedGroups, hasLabels ? selectedLabel : null);
					} else {
						if(hasLabels) filterGroups(selectedLabel);
						new ProcessData().processAverage(filteredData, granularity.getText(), showVU.isSelected(), selectedLabel);
					}
				} catch (Exception e) {
					showAlert(AlertType.ERROR, "Erro ao criar arquivo", e.getMessage(), "Contate o desenvolvedor.");
					e.printStackTrace();
				}
			}
		});

		Button fileImport = new Button("IMPORTAR OUTRO ARQUIVO");
		fileImport.setLayoutX(744);
		fileImport.setLayoutY(570);
		fileImport.setStyle("-fx-font-size: 14px;");
		fileImport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					importFile(group);
				} catch (ParseException | NumberFormatException e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "O formato do time stamp não foi reconhecido, ajuste o formato no arquivo config.properties.");
					e.printStackTrace();
				
				} catch (Exception e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "Verifique como o arquivo de entrada deve ser pelo botão 'Sobre'.");
					e.printStackTrace();
				}
			}
		});

		Button cancel = new Button("CANCELAR");
		cancel.setLayoutX(977);
		cancel.setLayoutY(570);
		cancel.setStyle("-fx-font-size: 14px;");
		cancel.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				drawHomePane();
			}
		});
		
		pane.getChildren().addAll(title, about, fileImport, cancel, subTitle, labelTitle, labelsSC, process, filterLabel);
	}

	private void drawGroups() {
		if (filteredData.length() == 0) return;
		Map<Integer, Integer> groups = getListOfGroups();
		VBox vbox = new VBox();
		vbox.setSpacing(3);
		vbox.setPadding(new Insets(5));
		for (Integer key : groups.keySet()) {
			CheckBox cb = new CheckBox();
			cb.setMinWidth(996);
			cb.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					if (cb.isSelected()) {
						selectedGroups.add(key);
						cb.setStyle("-fx-background-color:#e0e0e0;");
					} else {
						selectedGroups.remove(key);
					}
					process.setDisable(selectedGroups.size()==0);
				}
			});
			cb.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					cb.setStyle("-fx-background-color:#cccccc;");
				}
			});
			cb.setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					if (cb.isSelected()) {
						cb.setStyle("-fx-background-color:#e0e0e0;");
					} else {
						cb.setStyle("-fx-background-color: transparent;");
					}
				}
			});

			Pane row = new Pane();
			Label vu = new Label(key + " v.u.");
			vu.setMinWidth(498);
			vu.setMouseTransparent(true);
			vu.setAlignment(Pos.CENTER);

			Label qtd = new Label(groups.get(key) + " chamada" + (groups.get(key) > 1 ? "s" : ""));
			qtd.setMinWidth(498);
			qtd.setLayoutX(498);
			qtd.setAlignment(Pos.CENTER);
			qtd.setMouseTransparent(true);
			row.getChildren().addAll(cb, vu, qtd);
			vbox.getChildren().add(0, row);
		}
		groupsSC.setContent(vbox);
	}

	private void setIgnoreRampdown(boolean ignore) {
		ignoreRampdownFlag = ignore;
	}

	private void drawLabels(ScrollPane sc, String filter) {
		List<String> labels = getListOfLabels();
		hasLabels = labels != null;
		VBox vbox = new VBox();
		vbox.setPadding(new Insets(5));
		if (!hasLabels) {
			filteredData = data;
			Label noItems = new Label("O arquivo inserido não possui labels definidas.");
			noItems.setMinHeight(88);
			noItems.setMinWidth(1008);
			noItems.setAlignment(Pos.CENTER);
			vbox.getChildren().add(noItems);
			sc.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
			sc.setContent(vbox);
			drawGroups();
			return;
		} else {
			sc.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			if(group) labels.add(0, "[todos]");
		}
		vbox.setSpacing(3);
		final ToggleGroup tg = new ToggleGroup();
		boolean filterEmpty = true;
		for (String label : labels) {
			if(filter!=null && filter.length()>0 && label.equals("[todos]")) continue;
			if(filter!=null && filter.length()>0 && !label.toLowerCase().contains(filter.toLowerCase())) continue;
			filterEmpty = false;
			String name = label;
			if(textSize(name)>996) {
				name = resizeText(name, 980);
			}
			RadioButton rb = new RadioButton(name);
			rb.setId(label);
			rb.setMinWidth(996);
			rb.setMnemonicParsing(false);
			rb.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					rb.setStyle("-fx-background-color:#cccccc;");
					if(textSize(rb.getId())>996) {						
						animate(rb);
					}
				}
			});
			rb.setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent t) {
					if (rb.isSelected()) {
						rb.setStyle("-fx-background-color:#e0e0e0;");
					} else {
						rb.setStyle("-fx-background-color: transparent;");
					}
				}
			});
			rb.setStyle("-fx-hovered-background: #aaaaaa;");
			rb.setToggleGroup(tg);
			rb.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> obs, Boolean wasPreviouslySelected,
						Boolean isNowSelected) {
					if (isNowSelected) {
						selectedLabel = rb.getId();
						rb.setStyle("-fx-background-color:#e0e0e0;");
						if(group) {
							filterGroups(selectedLabel);
							drawGroups();							
						}
					} else {
						rb.setStyle("-fx-background-color: transparent;");
					}
					if(!group) {
						process.setDisable((selectedLabel.length()==0 && hasLabels) || graV<60);						
					}
				}
			});
			if(selectedLabel!=null && selectedLabel.length()>0 && selectedLabel.equals(label)) {
				rb.setSelected(true);
			}
			vbox.getChildren().add(rb);
		}
		if(filterEmpty && filter !=  null && filter.length()>0) {
			Label noResults = new Label("Seu filtro não retornou resultados, tente outro valor.");
			noResults.setMinHeight(88);
			noResults.setMinWidth(996);
			noResults.setAlignment(Pos.CENTER);
			vbox.getChildren().add(noResults);
		}
		sc.setContent(vbox);
	}
	
	private void animate(RadioButton rb) {
		String text = rb.getId();
		Thread taskThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int c = 0;
				while (rb.isHover()) {
					int wait = 75;
					String newText = text.substring(c, text.length() - 1);
					if (textSize(newText) < 970) {
						c = 0;
						wait = 2000;
					} else {
						newText = resizeText(newText, 970);
						c++;
					}
					try {
						Thread.sleep(wait);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					final String reportedNewText = newText;
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							rb.setText(reportedNewText);
						}
					});
				}
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						rb.setText(resizeText(text,980));
					}
				});
			}
		});
		taskThread.start();
	}
	
	private void filterGroups(String label) {
		if(label.equals("[todos]")) {
			filteredData = data;
			return;
		}
		filteredData = new JSONArray();
		for (int i = 0; i < data.length(); i++) {
			if (data.getJSONObject(i).getString("lb").equals(label)) {
				filteredData.put(data.getJSONObject(i));
			}
		}
	}
	
	private String resizeText(String myText, int expectedSize) {
		String currentText = myText;
		do {
			currentText = currentText.substring(0, currentText.length()-2);
		}while(textSize(currentText)>=expectedSize);
		return currentText;
	}
	
	private double textSize(String myText) {
		 final Text text = new Text(myText);
		 new Scene(new Group(text));
		 return text.getLayoutBounds().getWidth();
	}

	private void drawHomePane() {
		pane.getChildren().clear();
		Label title = new Label("JMGroupTool");
		title.setLayoutX(40);
		title.setLayoutY(10);
		title.setStyle("-fx-font-size: 20px;  -fx-font-weight: bold;");

		Button about = new Button("SOBRE");
		about.setLayoutX(1000);
		about.setLayoutY(12);
		about.setStyle("-fx-font-size: 14px;");
		about.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent arg0) {
				if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
				    try {
						Desktop.getDesktop().browse(new URI("https://github.com/carloscamposiki/JMGroupTool#jmgrouptool"));
					} catch (IOException | URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		Label groupTitle = new Label("Tempo médio e taxa de erro por grupo de v.u.");
		groupTitle.setLayoutX(40);
		groupTitle.setLayoutY(100);
		groupTitle.setMinWidth(470);
		groupTitle.setAlignment(Pos.CENTER);
		groupTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
		
		Label groupLabel = new Label(
					"Obtenha de um arquivo txt gerado pelo JMeter os dados de tempo médio e "
					+ "taxa de erro por quantidade de usuários simultâneos."
				);
		groupLabel.setLayoutX(40);
		groupLabel.setLayoutY(200);
		groupLabel.setMaxWidth(470);
		groupLabel.setWrapText(true);
		groupLabel.setStyle("-fx-font-size: 13px; -fx-text-alignment: center;");
		
		Button fileImport = new Button("IMPORTAR ARQUIVO");
		fileImport.setLayoutX(175);
		fileImport.setLayoutY(330);
		fileImport.setMinWidth(200);
		fileImport.setStyle("-fx-font-size: 14px;");
		fileImport.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					importFile(true);
				} catch (ParseException | NumberFormatException e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "O formato do time stamp não foi reconhecido, ajuste o formato no arquivo config.properties.");
					e.printStackTrace();
				} catch (Exception e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "Verifique como o arquivo de entrada deve ser pelo botão 'Sobre'.");
					e.printStackTrace();
				}
			}
		});

		Label importTip = new Label("Para mais detalhes clique no botão 'Sobre' no canto superior direito'");
		importTip.setMinWidth(318);
		importTip.setMaxWidth(250);
		importTip.setLayoutX(125);
		importTip.setLayoutY(373);
		importTip.setTextAlignment(TextAlignment.CENTER);
		importTip.setWrapText(true);

		Label groupTitle1 = new Label("Tempo médio no decorrer da execução");
		groupTitle1.setLayoutX(590);
		groupTitle1.setLayoutY(100);
		groupTitle1.setMinWidth(470);
		groupTitle1.setAlignment(Pos.CENTER);
		groupTitle1.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
		
		Label groupLabel1 = new Label(
					"Obtenha de um arquivo de texto gerado pelo JMeter o tempo médio "
					+ "de uma transação ao decorrer da execução em paralelo com a quantidade de usuários simultâneos."
				);
		groupLabel1.setLayoutX(590);
		groupLabel1.setLayoutY(200);
		groupLabel1.setMaxWidth(470);
		groupLabel1.setWrapText(true);
		groupLabel1.setStyle("-fx-font-size: 13px; -fx-text-alignment: center;");
		
		Button fileImport1 = new Button("IMPORTAR ARQUIVO");
		fileImport1.setLayoutX(725);
		fileImport1.setLayoutY(330);
		fileImport1.setMinWidth(200);
		fileImport1.setStyle("-fx-font-size: 14px;");
		fileImport1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				try {
					importFile(false);
				} catch (ParseException | NumberFormatException e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "O formato do time stamp não foi reconhecido, ajuste o formato no arquivo config.properties.");
					e.printStackTrace();
				} catch (Exception e) {
					showAlert(AlertType.ERROR, "Erro ao importar arquivo", e.getMessage(), "Verifique como o arquivo de entrada deve ser pelo botão 'Sobre'.");
					e.printStackTrace();
				}
			}
		});
		
		Label importTip1 = new Label("Para mais detalhes clique no botão 'Sobre' no canto superior direito'");
		importTip1.setMinWidth(318);
		importTip1.setMaxWidth(250);
		importTip1.setLayoutX(675);
		importTip1.setLayoutY(373);
		importTip1.setTextAlignment(TextAlignment.CENTER);
		importTip1.setWrapText(true);
	
		Separator separator = new Separator();
		separator.setOrientation(Orientation.VERTICAL);
		separator.setMinHeight(480);
		separator.setLayoutX(549);
		separator.setLayoutY(50);
		
		pane.getChildren().addAll(title, about, fileImport, importTip, groupLabel, separator, fileImport1, groupLabel1, importTip1, groupTitle, groupTitle1);
	}

	private void importFile(boolean isGroups) throws Exception {
		group = isGroups;
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Arquivos de texto (*.xml, *.txt, *.csv)", "*.xml", "*.txt", "*.csv"));
		file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			fileName = file.getName();
			fileParser.parseFile(file.getAbsolutePath());
			clear();
			data = fileParser.getDataList();
			drawFileLoadedPane();
		}
	}
	
	private void clear() {
		pane.getChildren().clear();
		groupsSC.setContent(null);
		data = null;
		filteredData = null;
		hasLabels = false;
		selectedLabel = "";
		ignoreRampdownFlag = true;
		selectedGroups = new ArrayList<Integer>();
	}

	private List<String> getListOfLabels() {
		if (!data.getJSONObject(0).has("lb")) {
			return null;
		}
		List<String> labels = new ArrayList<String>();
		for (int i = 0; i < data.length(); i++) {
			if (!labels.contains(data.getJSONObject(i).getString("lb"))) {
				labels.add(data.getJSONObject(i).getString("lb"));
			}
		}
		return labels;
	}

	private Map<Integer, Integer> getListOfGroups() {
		int max = 0;
		Map<Integer, Integer> groups = new TreeMap<Integer, Integer>();
		for (int i = 0; i < filteredData.length(); i++) {
			JSONObject item = filteredData.getJSONObject(i);
			int key = item.getInt("na");
			if (ignoreRampdownFlag) {
				if (key < max) {
					break;
				}
				max = key;
			}
			if (groups.keySet().contains(key)) {
				groups.put(key, groups.get(key) + 1);
			} else {
				groups.put(key, 1);
			}
		}
		return sortByValue(groups);
	}

	private <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
		List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
		list.sort(Entry.comparingByValue());

		Map<K, V> result = new LinkedHashMap<>();
		for (Entry<K, V> entry : list) {
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
	
	private void showAlert(AlertType type, String title, String subTitle, String desc) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(subTitle);
		alert.setContentText(desc);
		alert.show();
	}

	public Pane getPane() {
		return pane;
	}
}