package sample;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import sample.datamodel.ToDoData;
import sample.datamodel.ToDoItem;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;


public class Controller {
    private List<ToDoItem> toDoItems;
    @FXML
    private ListView<ToDoItem> toDoListView;
    @FXML
    private Label deadLinelabel;
    @FXML
    private TextArea itemDetails;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private ContextMenu listConextMenu;
    @FXML
    ToggleButton filterToggleButton;
    private FilteredList<ToDoItem> filteredList;
    private Predicate<ToDoItem> wantAllItems;
    private Predicate<ToDoItem> wantTodaysItems;



    public void initialize() {

        listConextMenu = new ContextMenu();
        MenuItem deleteMenuItem = new MenuItem("Delete");
        deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ToDoItem item = toDoListView.getSelectionModel().getSelectedItem();
                deleteItem(item);
            }
        });
        listConextMenu.getItems().addAll(deleteMenuItem);
        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observable, ToDoItem oldValue, ToDoItem newValue) {
                ToDoItem item = toDoListView.getSelectionModel().getSelectedItem();
                itemDetails.setText(item.getDetails());
                DateTimeFormatter df = DateTimeFormatter.ofPattern("MMMM d,yyyy");
                deadLinelabel.setText("Due: " + df.format(item.getDeadline()));
            }
        });

        wantAllItems=new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return true;
            }
        };
        wantTodaysItems=new Predicate<ToDoItem>() {
            @Override
            public boolean test(ToDoItem item) {
                return item.getDeadline().equals(LocalDate.now());
            }
        };


        filteredList = new FilteredList<>(ToDoData.getInstance().getToDoItems(),wantAllItems);
        SortedList<ToDoItem> sortedList = new SortedList<>(filteredList, new Comparator<ToDoItem>() {
            @Override
            public int compare(ToDoItem o1, ToDoItem o2) {
                return o1.getDeadline().compareTo(o2.getDeadline());
            }
        });

        toDoListView.setItems(sortedList);
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        toDoListView.getSelectionModel().selectFirst();

        toDoListView.setCellFactory(new Callback<ListView<ToDoItem>, ListCell<ToDoItem>>() {
            @Override
            public ListCell<ToDoItem> call(ListView<ToDoItem> param) {
                ListCell<ToDoItem> cell = new ListCell<ToDoItem>() {
                    @Override
                    protected void updateItem(ToDoItem item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item.getShortDescription());
                            if (item.getDeadline().isBefore(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.RED);
                            }
                            if (item.getDeadline().equals(LocalDate.now().plusDays(1))) {
                                setTextFill(Color.BROWN);
                            }
                        }
                    }
                };

                cell.emptyProperty().addListener(
                        (obs, wasEmpty, isNowEmpty) -> {
                            if (isNowEmpty) {
                                cell.setContextMenu(null);
                            } else {
                                cell.setContextMenu(listConextMenu);
                            }
                        }
                );


                return cell;
            }
        });

    }


    public void deleteItem(ToDoItem item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete To Do Item");
        alert.setHeaderText("Delete item " + item.getShortDescription());
        alert.setContentText("Are you sure");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ToDoData.getInstance().deleteTodoitem(item);
        }
    }

    @FXML
    public void showNewItemDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("todoItemDialog.fxml"));
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Add new TodoItem");
        dialog.setHeaderText("Use this dialog to create a new todo item");

        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            DialogController controller = fxmlLoader.getController();
            ToDoItem newItem = controller.processResults();
//            toDoListView.getItems().setAll(ToDoData.getInstance().getToDoItems());
            toDoListView.getSelectionModel().select(newItem);
            System.out.println("Ok pressed");
        } else {
            System.out.println("Cancel pressed");
        }

    }


    @FXML
    public void handleClickListView() {
        ToDoItem toDoItem = toDoListView.getSelectionModel().getSelectedItem();
        itemDetails.setText(toDoItem.getDetails());
        //        StringBuilder sb=new StringBuilder(toDoItem.getDetails());
//        sb.append("\n\n\n\n");
//        sb.append(toDoItem.getDeadline());
//        itemDetails.setText(sb.toString());

    }

    @FXML
    public void handleKeyPressed(javafx.scene.input.KeyEvent keyEvent) {
        ToDoItem selectedItem = toDoListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            if (keyEvent.getCode().equals(KeyCode.DELETE)) {
                deleteItem(selectedItem);
            }
        }
    }

    @FXML
    public void handleFilterButton(ActionEvent actionEvent) {
        ToDoItem selectedItem=toDoListView.getSelectionModel().getSelectedItem();
        if (filterToggleButton.isSelected()) {
            filteredList.setPredicate(wantTodaysItems);
            toDoListView.getSelectionModel().select(selectedItem);
            if (filteredList.isEmpty()) {
                itemDetails.clear();
            } else if(filteredList.contains(selectedItem)) {
                toDoListView.getSelectionModel().select(selectedItem);
            } else {
                toDoListView.getSelectionModel().selectFirst();
            }



        } else {
            filteredList.setPredicate(wantAllItems);
            toDoListView.getSelectionModel().select(selectedItem);
        }
    }
    @FXML
    public void handleExit(ActionEvent actionEvent) {
        Platform.exit();
    }
}
