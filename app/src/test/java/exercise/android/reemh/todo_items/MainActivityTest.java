package exercise.android.reemh.todo_items;

import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.lang.reflect.Field;
import java.util.ArrayList;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest extends TestCase {

  private ActivityController<MainActivity> activityController;
  private TodoItemsStore mockHolder;

  @Before
  public void setup(){
    mockHolder = Mockito.mock(TodoItemsStore.class);

    // Use reflection to override the singleton instance
    try {
      Field instance = TodoItemsStore.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(instance, mockHolder);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    // when asking the `mockHolder` to get the current items, return an empty list
    Mockito.when(mockHolder.getCurrentItems())
      .thenReturn(new ArrayList<>());

    activityController = Robolectric.buildActivity(MainActivity.class);
  }

  @After
  public void tearDown(){
    try {
      Field instance = TodoItemsStore.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(instance, null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void when_activityIsLaunched_then_theEditTextStartsEmpty() {
    // setup
    activityController.create().visible();
    MainActivity activityUnderTest = activityController.get();
    EditText editText = activityUnderTest.findViewById(R.id.editTextInsertTask);
    String userInput = editText.getText().toString();
    // verify
    assertTrue(userInput.isEmpty());
  }

  @Test
  public void when_userPutInputAndClicksButton_then_activityShouldCallAddItem() {
    // setup
    String userInput = "Call my grandma today at 18:00";
    activityController.create().visible(); // let the activity think it is being shown
    MainActivity activityUnderTest = activityController.get();
    EditText editText = activityUnderTest.findViewById(R.id.editTextInsertTask);
    View fab = activityUnderTest.findViewById(R.id.buttonCreateTodoItem);

    // test - mock user interactions
    editText.setText(userInput);
    fab.performClick();

    // verify: verify that `mockHolder.addNewInProgressItem()` was called, with exactly same string
    Mockito.verify(mockHolder).addNewInProgressItem(eq(userInput));
  }

  @Test
  public void when_userPutInputAndClicksButton_then_inputShouldBeErasedFromEditText() {
    // setup
    String userInput = "Call my grandma today at 18:00";
    activityController.create().visible(); // let the activity think it is being shown
    MainActivity activityUnderTest = activityController.get();
    EditText editText = activityUnderTest.findViewById(R.id.editTextInsertTask);
    View fab = activityUnderTest.findViewById(R.id.buttonCreateTodoItem);

    // test - mock user interactions
    editText.setText(userInput);
    fab.performClick();

    // verify: verify that `mockHolder.addNewInProgressItem()` was called, with exactly same string
    assertTrue(TextUtils.isEmpty(editText.getText()));
  }

  @Test
  public void when_holderSaysNoItems_then_recyclerViewShowsZeroItems() {
    // setup
    Mockito.when(mockHolder.getCurrentItems())
      .thenReturn(new ArrayList<>());

    // test - let the activity think it is being shown
    activityController.create().visible();

    // verify
    MainActivity activityUnderTest = activityController.get();
    RecyclerView recyclerView = activityUnderTest.findViewById(R.id.recyclerTodoItemsList);
    RecyclerView.Adapter adapter = recyclerView.getAdapter();
    assertNotNull(adapter);
    assertEquals(0, adapter.getItemCount());
  }

  @Test
  public void when_holderSays1ItemOfTypeInProgress_then_activityShouldShow1MatchingViewInRecyclerView(){
    // setup

    // when asking the `mockHolder` to get the current items, return a list with 1 item of type "in progress"
    ArrayList<TodoItem> itemsReturnedByHolder = new ArrayList<>();
    Mockito.when(mockHolder.getCurrentItems()).thenReturn(itemsReturnedByHolder);
    String expected = "do homework";
    TodoItem itemInProgress = new TodoItem(expected, false);
    itemsReturnedByHolder.add(itemInProgress);

    // test - let the activity think it is being shown
    activityController.create().visible();

    // verify: make sure that the activity shows a matching subview in the recycler view
    MainActivity activityUnderTest = activityController.get();
    RecyclerView recyclerView = activityUnderTest.findViewById(R.id.recyclerTodoItemsList);

    // 1. verify that adapter says there should be 1 item showing
    RecyclerView.Adapter adapter = recyclerView.getAdapter();
    assertNotNull(adapter);
    assertEquals(1, adapter.getItemCount());

    // 2. verify that the shown view has a checkbox being not-checked and has a TextView showing the correct description
    View viewInRecycler = recyclerView.findViewHolderForAdapterPosition(0).itemView;
    TextView todoText = viewInRecycler.findViewById(R.id.text_todo);
    CheckBox todoCheckbox = viewInRecycler.findViewById(R.id.checkbox_todo);
    assertFalse(todoCheckbox.isChecked());
    assertEquals(todoText.getText().toString(), expected);
  }


  @Test
  public void when_holderSays1ItemOfTypeDone_then_activityShouldShow1MatchingViewInRecyclerView(){
    // setup

    // when asking the `mockHolder` to get the current items, return a list with 1 item of type "DONE"
    ArrayList<TodoItem> itemsReturnedByHolder = new ArrayList<>();
    Mockito.when(mockHolder.getCurrentItems())
      .thenReturn(itemsReturnedByHolder);
    String expected = "buy tomatoes";
    TodoItem itemDone = new TodoItem(expected, true);
    itemsReturnedByHolder.add(itemDone);

    // test - let the activity think it is being shown
    activityController.create().visible();

    // verify: make sure that the activity shows a matching subview in the recycler view
    MainActivity activityUnderTest = activityController.get();
    RecyclerView recyclerView = activityUnderTest.findViewById(R.id.recyclerTodoItemsList);

    // 1. verify that adapter says there should be 1 item showing
    RecyclerView.Adapter adapter = recyclerView.getAdapter();
    assertNotNull(adapter);
    assertEquals(1, adapter.getItemCount());

    // 2. verify that the shown view has a checkbox being checked and has a TextView showing the correct description
    View viewInRecycler = recyclerView.findViewHolderForAdapterPosition(0).itemView;
    TextView todoText = viewInRecycler.findViewById(R.id.text_todo);
    CheckBox todoCheckbox = viewInRecycler.findViewById(R.id.checkbox_todo);
    assertTrue(todoCheckbox.isChecked());
    assertEquals(todoText.getText().toString(), expected);
  }
}
