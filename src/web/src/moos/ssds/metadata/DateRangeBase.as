/**
 * Generated by Gas3 v2.1.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (DateRange.as).
 */

package moos.ssds.metadata {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;

    [Bindable]
    public class DateRangeBase implements IExternalizable, IDateRange {

        private var _dateRange:IDateRange;
        private var _endDate:Date;
        private var _proxy:Boolean;
        private var _startDate:Date;

        public function set dateRange(value:IDateRange):void {
            _dateRange = value;
        }
        public function get dateRange():IDateRange {
            return _dateRange;
        }

        public function set endDate(value:Date):void {
            _endDate = value;
        }
        public function get endDate():Date {
            return _endDate;
        }

        public function set startDate(value:Date):void {
            _startDate = value;
        }
        public function get startDate():Date {
            return _startDate;
        }

        public function readExternal(input:IDataInput):void {
            _dateRange = input.readObject() as IDateRange;
            _endDate = input.readObject() as Date;
            _proxy = input.readObject() as Boolean;
            _startDate = input.readObject() as Date;
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_dateRange);
            output.writeObject(_endDate);
            output.writeObject(_proxy);
            output.writeObject(_startDate);
        }
    }
}