/**
 * Generated by Gas3 v2.1.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (Event.as).
 */

package moos.ssds.metadata {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;

    [Bindable]
    public class EventBase implements IExternalizable, IMetadataObject, IDescription, IDateRange {

        private var _description:String;
        private var _endDate:Date;
        private var _id:Number;
        private var _name:String;
        private var _startDate:Date;
        private var _version:Number;

        public function set description(value:String):void {
            _description = value;
        }
        public function get description():String {
            return _description;
        }

        public function set endDate(value:Date):void {
            _endDate = value;
        }
        public function get endDate():Date {
            return _endDate;
        }

        public function set id(value:Number):void {
            _id = value;
        }
        public function get id():Number {
            return _id;
        }

        public function set name(value:String):void {
            _name = value;
        }
        public function get name():String {
            return _name;
        }

        public function set startDate(value:Date):void {
            _startDate = value;
        }
        public function get startDate():Date {
            return _startDate;
        }

        public function set version(value:Number):void {
            _version = value;
        }
        public function get version():Number {
            return _version;
        }

        public function get dateRange():IDateRange {
            return null;
        }

        public function readExternal(input:IDataInput):void {
            _description = input.readObject() as String;
            _endDate = input.readObject() as Date;
            _id = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _name = input.readObject() as String;
            _startDate = input.readObject() as Date;
            _version = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_description);
            output.writeObject(_endDate);
            output.writeObject(_id);
            output.writeObject(_name);
            output.writeObject(_startDate);
            output.writeObject(_version);
        }
    }
}