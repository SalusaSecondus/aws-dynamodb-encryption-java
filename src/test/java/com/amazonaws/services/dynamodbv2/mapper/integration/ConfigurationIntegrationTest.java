/*
 * Copyright 2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except
 * in compliance with the License. A copy of the License is located at
 * 
 * http://aws.amazon.com/apache2.0
 * 
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.amazonaws.services.dynamodbv2.mapper.integration;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAutoGeneratedKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.mapper.encryption.NumberAttributeTestClass;
import com.amazonaws.services.dynamodbv2.mapper.encryption.TestDynamoDBMapperFactory;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;


/**
 * Tests of the configuration object
 */
public class ConfigurationIntegrationTest extends DynamoDBMapperCryptoIntegrationTestBase {

    // We don't start with the current system millis like other tests because
    // it's out of the range of some data types
    private static int start = 1;
    private static int byteStart = -127;

    @Test
    public void testClobber() throws Exception {
        DynamoDBMapper util = new DynamoDBMapper(dynamo, new DynamoDBMapperConfig(SaveBehavior.CLOBBER));

        NumberAttributeTestClassExtended obj = getUniqueObject();
        util.save(obj);
        assertEquals(obj, util.load(obj.getClass(), obj.getKey()));

        NumberAttributeTestClass copy = copy(obj);
        util.save(copy);
        assertEquals(copy, util.load(copy.getClass(), obj.getKey()));

        // We should have lost the extra field because of the clobber behavior
        assertNull(util.load(NumberAttributeTestClassExtended.class, obj.getKey()).getExtraField());

        // Now test overriding the clobber behavior on a per-save basis
        obj = getUniqueObject();
        util.save(obj);
        assertEquals(obj, util.load(obj.getClass(), obj.getKey()));

        copy = copy(obj);
        util.save(copy, new DynamoDBMapperConfig(SaveBehavior.UPDATE));
        assertEquals(copy, util.load(copy.getClass(), obj.getKey()));

        // We shouldn't have lost any extra info
        assertNotNull(util.load(NumberAttributeTestClassExtended.class, obj.getKey()).getExtraField());
    }

    @Test
    public void testTableOverride() throws Exception {
        DynamoDBMapper util = TestDynamoDBMapperFactory.createDynamoDBMapper(dynamo);

        TableOverrideTestClass obj = new TableOverrideTestClass();
        obj.setOtherField(UUID.randomUUID().toString());

        try {
            util.save(obj);
            fail("Expected an exception");
        } catch ( Exception e ) {
        }

        util.save(obj, new DynamoDBMapperConfig(new TableNameOverride("aws-java-sdk-util-crypto")));

        try {
            util.load(TableOverrideTestClass.class, obj.getKey());
            fail("Expected an exception");
        } catch ( Exception e ) {
        }
        
        Object loaded =  util.load(TableOverrideTestClass.class, obj.getKey(), new DynamoDBMapperConfig(TableNameOverride.withTableNamePrefix("aws-")));
        assertEquals(loaded, obj);
        
        try {
            util.delete(obj);
            fail("Expected an exception");
        } catch ( Exception e ) {
        }
        
        util.delete(obj, new DynamoDBMapperConfig(TableNameOverride.withTableNamePrefix("aws-")));
    }

    private NumberAttributeTestClassExtended getUniqueObject() {
        NumberAttributeTestClassExtended obj = new NumberAttributeTestClassExtended();
        obj.setKey(String.valueOf(startKey++));
        obj.setBigDecimalAttribute(new BigDecimal(startKey++));
        obj.setBigIntegerAttribute(new BigInteger("" + startKey++));
        obj.setByteAttribute((byte) byteStart++);
        obj.setByteObjectAttribute(new Byte("" + byteStart++));
        obj.setDoubleAttribute(new Double("" + start++));
        obj.setDoubleObjectAttribute(new Double("" + start++));
        obj.setFloatAttribute(new Float("" + start++));
        obj.setFloatObjectAttribute(new Float("" + start++));
        obj.setIntAttribute(new Integer("" + start++));
        obj.setIntegerAttribute(new Integer("" + start++));
        obj.setLongAttribute(new Long("" + start++));
        obj.setLongObjectAttribute(new Long("" + start++));
        obj.setDateAttribute(new Date(startKey++));
        obj.setBooleanAttribute(start++ % 2 == 0);
        obj.setBooleanObjectAttribute(start++ % 2 == 0);
        obj.setExtraField("" + startKey++);
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date(startKey++));
        obj.setCalendarAttribute(cal);
        return obj;
    }

    private NumberAttributeTestClass copy(NumberAttributeTestClassExtended obj) {
        NumberAttributeTestClass copy = new NumberAttributeTestClass();
        copy.setKey(obj.getKey());
        copy.setBigDecimalAttribute(obj.getBigDecimalAttribute());
        copy.setBigIntegerAttribute(obj.getBigIntegerAttribute());
        copy.setByteAttribute(obj.getByteAttribute());
        copy.setByteObjectAttribute(obj.getByteObjectAttribute());
        copy.setDoubleAttribute(obj.getDoubleAttribute());
        copy.setDoubleObjectAttribute(obj.getDoubleObjectAttribute());
        copy.setFloatAttribute(obj.getFloatAttribute());
        copy.setFloatObjectAttribute(obj.getFloatObjectAttribute());
        copy.setIntAttribute(obj.getIntAttribute());
        copy.setIntegerAttribute(obj.getIntegerAttribute());
        copy.setLongAttribute(obj.getLongAttribute());
        copy.setLongObjectAttribute(obj.getLongObjectAttribute());
        copy.setDateAttribute(obj.getDateAttribute());
        copy.setBooleanAttribute(obj.isBooleanAttribute());
        copy.setBooleanObjectAttribute(obj.getBooleanObjectAttribute());
        return copy;
    }

    @DynamoDBTable(tableName = "aws-java-sdk-util-crypto")
    public static final class NumberAttributeTestClassExtended extends NumberAttributeTestClass {

        private String extraField;

        public String getExtraField() {
            return extraField;
        }

        public void setExtraField(String extraField) {
            this.extraField = extraField;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + ((extraField == null) ? 0 : extraField.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( !super.equals(obj) )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            NumberAttributeTestClassExtended other = (NumberAttributeTestClassExtended) obj;
            if ( extraField == null ) {
                if ( other.extraField != null )
                    return false;
            } else if ( !extraField.equals(other.extraField) )
                return false;
            return true;
        }
    }

    @DynamoDBTable(tableName = "java-sdk-util-crypto") // doesn't exist
    public static final class TableOverrideTestClass {

        private String key;
        private String otherField;

        @DynamoDBAutoGeneratedKey
        @DynamoDBHashKey
        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getOtherField() {
            return otherField;
        }

        public void setOtherField(String otherField) {
            this.otherField = otherField;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((key == null) ? 0 : key.hashCode());
            result = prime * result + ((otherField == null) ? 0 : otherField.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if ( this == obj )
                return true;
            if ( obj == null )
                return false;
            if ( getClass() != obj.getClass() )
                return false;
            TableOverrideTestClass other = (TableOverrideTestClass) obj;
            if ( key == null ) {
                if ( other.key != null )
                    return false;
            } else if ( !key.equals(other.key) )
                return false;
            if ( otherField == null ) {
                if ( other.otherField != null )
                    return false;
            } else if ( !otherField.equals(other.otherField) )
                return false;
            return true;
        }

    }

}
