package com.ismolka.validation.test.change;

import com.ismolka.validation.test.config.TestConfig;
import com.ismolka.validation.utils.change.CheckerResult;
import com.ismolka.validation.utils.change.Difference;
import com.ismolka.validation.utils.change.collection.CollectionElementDifference;
import com.ismolka.validation.utils.change.collection.DefaultCollectionChangesCheckerBuilder;
import com.ismolka.validation.utils.change.map.DefaultMapChangesCheckerBuilder;
import com.ismolka.validation.utils.change.map.MapElementDifference;
import com.ismolka.validation.utils.change.value.DefaultValueChangesCheckerBuilder;
import com.ismolka.validation.utils.change.value.ValueCheckDescriptorBuilder;
import com.ismolka.validation.utils.change.value.ValueDifference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest(classes = TestConfig.class)
@EnableAutoConfiguration
public class ChangeTest {

    private static final String OLD_VAL_STR = "OLD VALUE";

    private static final String NEW_VAL_STR = "NEW VALUE";

    @Test
    public void test_simpleField() {
        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        oldTestObj.setSimpleField(OLD_VAL_STR);
        newTestObj.setSimpleField(NEW_VAL_STR);

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addGlobalEqualsField("simpleField")
                .build().getResult(oldTestObj, newTestObj);

        ValueDifference<?> valueDifference = result.navigator().getDifference("simpleField").unwrap(ValueDifference.class);

        String oldValueFromCheckResult = (String) valueDifference.oldValue();
        String newValueFromCheckResult = (String) valueDifference.newValue();

        Assertions.assertEquals(oldValueFromCheckResult, oldTestObj.getSimpleField());
        Assertions.assertEquals(newValueFromCheckResult, newTestObj.getSimpleField());
    }

    @Test
    public void test_innerObject() {
        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        oldTestObj.setInnerObject(new ChangeTestInnerObject(OLD_VAL_STR));
        newTestObj.setInnerObject(new ChangeTestInnerObject(NEW_VAL_STR));

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addAttributeToCheck(
                        ValueCheckDescriptorBuilder.builder(ChangeTestObject.class, ChangeTestInnerObject.class)
                                .attribute("innerObject")
                                .addEqualsField("valueFromObject")
                                .build()
                )
                .build().getResult(oldTestObj, newTestObj);

        ValueDifference<?> valueDifference = result.navigator().getDifference("innerObject.valueFromObject").unwrap(ValueDifference.class);

        String oldValueFromCheckResult = (String) valueDifference.oldValue();
        String newValueFromCheckResult = (String) valueDifference.newValue();

        Assertions.assertEquals(oldValueFromCheckResult, oldTestObj.getInnerObject().getValueFromObject());
        Assertions.assertEquals(newValueFromCheckResult, newTestObj.getInnerObject().getValueFromObject());
    }

    @Test
    public void test_innerObjectWithoutValueDescriptor() {
        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        oldTestObj.setInnerObject(new ChangeTestInnerObject(OLD_VAL_STR));
        newTestObj.setInnerObject(new ChangeTestInnerObject(NEW_VAL_STR));

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addGlobalEqualsField("innerObject.valueFromObject")
                .build().getResult(oldTestObj, newTestObj);

        ValueDifference<?> valueDifference = result.navigator().getDifference("innerObject.valueFromObject").unwrap(ValueDifference.class);

        String oldValueFromCheckResult = (String) valueDifference.oldValue();
        String newValueFromCheckResult = (String) valueDifference.newValue();

        Assertions.assertEquals(oldValueFromCheckResult, oldTestObj.getInnerObject().getValueFromObject());
        Assertions.assertEquals(newValueFromCheckResult, newTestObj.getInnerObject().getValueFromObject());
    }

    @Test
    public void test_collection() {
        String key = "ID_IN_COLLECTION";

        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        ChangeTestObjectCollection oldCollectionObj = new ChangeTestObjectCollection(key, OLD_VAL_STR);
        ChangeTestObjectCollection newCollectionObj = new ChangeTestObjectCollection(key, NEW_VAL_STR);

        oldTestObj.setCollection(List.of(oldCollectionObj));
        newTestObj.setCollection(List.of(newCollectionObj));

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addAttributeToCheck(
                        ValueCheckDescriptorBuilder.builder(ChangeTestObject.class, ChangeTestObjectCollection.class)
                                .attribute("collection")
                                .changesChecker(
                                        DefaultCollectionChangesCheckerBuilder.builder(ChangeTestObjectCollection.class)
                                                .addGlobalEqualsField("valueFromCollection")
                                                .addFieldForMatching("key")
                                                .build()
                                ).build()

                ).build().getResult(oldTestObj, newTestObj);

        CollectionElementDifference<ChangeTestObjectCollection> difference = result.navigator().getDifferenceForCollection("collection", ChangeTestObjectCollection.class).stream().findFirst().orElseThrow(() -> new RuntimeException("Result for collection is not present"));

        Assertions.assertEquals(difference.elementFromOldCollection().getValueFromCollection(), oldCollectionObj.getValueFromCollection());
        Assertions.assertEquals(difference.elementFromNewCollection().getValueFromCollection(), newCollectionObj.getValueFromCollection());
    }

    @Test
    public void test_array() {
        String key = "ID_IN_COLLECTION";

        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        ChangeTestObjectCollection oldCollectionObj = new ChangeTestObjectCollection(key, OLD_VAL_STR);
        ChangeTestObjectCollection newCollectionObj = new ChangeTestObjectCollection(key, NEW_VAL_STR);

        oldTestObj.setArray(new ChangeTestObjectCollection[] { oldCollectionObj });
        newTestObj.setArray(new ChangeTestObjectCollection[] { newCollectionObj });

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addAttributeToCheck(
                        ValueCheckDescriptorBuilder.builder(ChangeTestObject.class, ChangeTestObjectCollection.class)
                                .attribute("array")
                                .changesChecker(
                                        DefaultCollectionChangesCheckerBuilder.builder(ChangeTestObjectCollection.class)
                                                .addGlobalEqualsField("valueFromCollection")
                                                .addFieldForMatching("key")
                                                .build()
                                ).build()

                ).build().getResult(oldTestObj, newTestObj);

        CollectionElementDifference<ChangeTestObjectCollection> difference = result.navigator().getDifferenceForCollection("array", ChangeTestObjectCollection.class).stream().findFirst().orElseThrow(() -> new RuntimeException("Result for collection is not present"));

        Assertions.assertEquals(difference.elementFromOldCollection().getValueFromCollection(), oldCollectionObj.getValueFromCollection());
        Assertions.assertEquals(difference.elementFromNewCollection().getValueFromCollection(), newCollectionObj.getValueFromCollection());
    }

    @Test
    public void test_map() {
        String key = "ID_IN_MAP";

        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        ChangeTestObjectMap oldMapObj = new ChangeTestObjectMap(OLD_VAL_STR);
        ChangeTestObjectMap newMapObj = new ChangeTestObjectMap(NEW_VAL_STR);

        oldTestObj.setMap(Map.of(key, oldMapObj));
        newTestObj.setMap(Map.of(key, newMapObj));

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addAttributeToCheck(
                        ValueCheckDescriptorBuilder.builder(ChangeTestObject.class, ChangeTestObjectMap.class)
                                .attribute("map")
                                .changesChecker(
                                        DefaultMapChangesCheckerBuilder.builder(String.class, ChangeTestObjectMap.class)
                                                .addGlobalEqualsField("valueFromMap")
                                                .build()
                                ).build()
                ).build().getResult(oldTestObj, newTestObj);

        MapElementDifference<String, ChangeTestObjectMap> difference = result.navigator().getDifferenceForMap("map", String.class, ChangeTestObjectMap.class).stream().findFirst().orElseThrow(() -> new RuntimeException("Result for map is not present"));

        Assertions.assertEquals(difference.elementFromOldMap().getValueFromMap(), oldMapObj.getValueFromMap());
        Assertions.assertEquals(difference.elementFromNewMap().getValueFromMap(), newMapObj.getValueFromMap());
    }

    @Test
    public void test_wrongNavigate() {
        ChangeTestObject oldTestObj = new ChangeTestObject();
        ChangeTestObject newTestObj = new ChangeTestObject();

        oldTestObj.setInnerObject(new ChangeTestInnerObject(OLD_VAL_STR));
        newTestObj.setInnerObject(new ChangeTestInnerObject(NEW_VAL_STR));

        CheckerResult result = DefaultValueChangesCheckerBuilder.builder(ChangeTestObject.class)
                .addGlobalEqualsField("innerObject.valueFromObject")
                .build().getResult(oldTestObj, newTestObj);

        Difference difference = result.navigator().getDifference("innerObject.valueFromObject111.test");

        Assertions.assertNull(difference);
    }
}
