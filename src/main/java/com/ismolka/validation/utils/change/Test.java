package com.ismolka.validation.utils.change;

import com.ismolka.validation.utils.change.attribute.AttributeChangesCheckerResult;
import com.ismolka.validation.utils.change.attribute.AttributeMetaInfo;
import com.ismolka.validation.utils.change.attribute.DefaultAttributeChangesChecker;

public class Test {

    public static void main(String[] args) {

        TestClass first = new TestClass("1", "2");
        TestClass second = new TestClass("3", "4");

        DefaultAttributeChangesChecker changesChecker = DefaultAttributeChangesChecker.builder()
                .addAttributeToCheck(AttributeMetaInfo.builder(TestClass.class)
                        .field("foo")
                        .build())
                .addAttributeToCheck(AttributeMetaInfo.builder(TestClass.class)
                        .field("bar")
                        .build())
                .build();

        AttributeChangesCheckerResult attributeChangesCheckerResult = changesChecker.getResult(first, second);

        attributeChangesCheckerResult = attributeChangesCheckerResult;
    }

    public static class TestClass {
        private String foo;
        private String bar;

        public TestClass(String foo, String bar) {
            this.foo = foo;
            this.bar = bar;
        }

        public String getFoo() {
            return foo;
        }

        public void setFoo(String foo) {
            this.foo = foo;
        }

        public String getBar() {
            return bar;
        }

        public void setBar(String bar) {
            this.bar = bar;
        }
    }
}
