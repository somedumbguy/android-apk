package com.example.hello.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.hello.domain.model.SetRecord;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class SetRecordCodecTest {

    @Test
    public void roundTrips() {
        List<SetRecord> sets = Arrays.asList(new SetRecord(192.5, 5, 5), new SetRecord(50, 11, 8));
        assertEquals(sets, SetRecordCodec.decode(SetRecordCodec.encode(sets)));
    }

    @Test
    public void blankOrMalformedInputGivesAnEmptyList() {
        assertTrue(SetRecordCodec.decode(null).isEmpty());
        assertTrue(SetRecordCodec.decode("").isEmpty());
        assertTrue(SetRecordCodec.decode("garbage").isEmpty());
        assertTrue(SetRecordCodec.decode("190:5").isEmpty());
    }
}
