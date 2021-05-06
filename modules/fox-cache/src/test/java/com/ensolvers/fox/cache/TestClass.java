package com.ensolvers.fox.cache;

public class TestClass {
    Long id;
    String stringValue;
    Integer integerValue;
    Long longValue;

    public TestClass(Long id, String stringValue, Integer integerValue, Long longValue) {
        this.id = id;
        this.stringValue = stringValue;
        this.integerValue = integerValue;
        this.longValue = longValue;
    }

    protected TestClass() {
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getIntegerValue() {
        return integerValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestClass testClass = (TestClass) o;

        if (!id.equals(testClass.id)) return false;
        if (!stringValue.equals(testClass.stringValue)) return false;
        if (!integerValue.equals(testClass.integerValue)) return false;
        return longValue.equals(testClass.longValue);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + stringValue.hashCode();
        result = 31 * result + integerValue.hashCode();
        result = 31 * result + longValue.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TestClass{" +
                "id=" + id +
                ", stringValue='" + stringValue + '\'' +
                ", integerValue=" + integerValue +
                ", longValue=" + longValue +
                '}';
    }
}
