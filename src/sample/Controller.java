package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import sample.datamodel.ToDoData;
import sample.datamodel.ToDoItem;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class Controller {
    private List<ToDoItem> toDoItems;
    @FXML
    private ListView<ToDoItem> toDoListView;
    @FXML
    private Label deadLinelabel;
    @FXML
    private TextArea itemDetails;

    public void initialize() {



        toDoListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ToDoItem>() {
            @Override
            public void changed(ObservableValue<? extends ToDoItem> observable, ToDoItem oldValue, ToDoItem newValue) {
                ToDoItem item=toDoListView.getSelectionModel().getSelectedItem();
                itemDetails.setText(item.getDetails());
                DateTimeFormatter df=DateTimeFormatter.ofPattern("MMMM d,yyyy");
                deadLinelabel.setText("Due: " + df.format(item.getDeadline()));
            }
        });


        toDoListView.getItems().setAll(ToDoData.getInstance().getToDoItems());
        toDoListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        toDoListView.getSelectionModel().selectFirst();


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


}
