/**
 * Generated by Gas3 v2.1.0 (Granite Data Services).
 *
 * WARNING: DO NOT CHANGE THIS FILE. IT MAY BE OVERWRITTEN EACH TIME YOU USE
 * THE GENERATOR. INSTEAD, EDIT THE INHERITED CLASS (DataProducer.as).
 */

package moos.ssds.metadata {

    import flash.utils.IDataInput;
    import flash.utils.IDataOutput;
    import flash.utils.IExternalizable;
    import mx.collections.ListCollectionView;

    [Bindable]
    public class DataProducerBase implements IExternalizable, IMetadataObject, IDescription, IResourceOwner, IDateRange {

        private var _childDataProducers:ListCollectionView;
        private var _dataProducerGroups:ListCollectionView;
        private var _dataProducerType:String;
        private var _description:String;
        private var _device:Device;
        private var _endDate:Date;
        private var _events:ListCollectionView;
        private var _hostName:String;
        private var _id:Number;
        private var _inputs:ListCollectionView;
        private var _keywords:ListCollectionView;
        private var _name:String;
        private var _nominalBenthicAltitude:Number;
        private var _nominalBenthicAltitudeAccuracy:Number;
        private var _nominalDepth:Number;
        private var _nominalDepthAccuracy:Number;
        private var _nominalLatitude:Number;
        private var _nominalLatitudeAccuracy:Number;
        private var _nominalLongitude:Number;
        private var _nominalLongitudeAccuracy:Number;
        private var _orientationDescription:String;
        private var _outputs:ListCollectionView;
        private var _parentDataProducer:DataProducer;
        private var _person:Person;
        private var _resources:ListCollectionView;
        private var _role:String;
        private var _software:Software;
        private var _startDate:Date;
        private var _version:Number;
        private var _x3DOrientationText:String;
        private var _xoffset:Number;
        private var _yoffset:Number;
        private var _zoffset:Number;

        public function set childDataProducers(value:ListCollectionView):void {
            _childDataProducers = value;
        }
        public function get childDataProducers():ListCollectionView {
            return _childDataProducers;
        }

        public function set dataProducerGroups(value:ListCollectionView):void {
            _dataProducerGroups = value;
        }
        public function get dataProducerGroups():ListCollectionView {
            return _dataProducerGroups;
        }

        public function set dataProducerType(value:String):void {
            _dataProducerType = value;
        }
        public function get dataProducerType():String {
            return _dataProducerType;
        }

        public function set description(value:String):void {
            _description = value;
        }
        public function get description():String {
            return _description;
        }

        public function set device(value:Device):void {
            _device = value;
        }
        public function get device():Device {
            return _device;
        }

        public function set endDate(value:Date):void {
            _endDate = value;
        }
        public function get endDate():Date {
            return _endDate;
        }

        public function set events(value:ListCollectionView):void {
            _events = value;
        }
        public function get events():ListCollectionView {
            return _events;
        }

        public function set hostName(value:String):void {
            _hostName = value;
        }
        public function get hostName():String {
            return _hostName;
        }

        public function set id(value:Number):void {
            _id = value;
        }
        public function get id():Number {
            return _id;
        }

        public function set inputs(value:ListCollectionView):void {
            _inputs = value;
        }
        public function get inputs():ListCollectionView {
            return _inputs;
        }

        public function set keywords(value:ListCollectionView):void {
            _keywords = value;
        }
        public function get keywords():ListCollectionView {
            return _keywords;
        }

        public function set name(value:String):void {
            _name = value;
        }
        public function get name():String {
            return _name;
        }

        public function set nominalBenthicAltitude(value:Number):void {
            _nominalBenthicAltitude = value;
        }
        public function get nominalBenthicAltitude():Number {
            return _nominalBenthicAltitude;
        }

        public function set nominalBenthicAltitudeAccuracy(value:Number):void {
            _nominalBenthicAltitudeAccuracy = value;
        }
        public function get nominalBenthicAltitudeAccuracy():Number {
            return _nominalBenthicAltitudeAccuracy;
        }

        public function set nominalDepth(value:Number):void {
            _nominalDepth = value;
        }
        public function get nominalDepth():Number {
            return _nominalDepth;
        }

        public function set nominalDepthAccuracy(value:Number):void {
            _nominalDepthAccuracy = value;
        }
        public function get nominalDepthAccuracy():Number {
            return _nominalDepthAccuracy;
        }

        public function set nominalLatitude(value:Number):void {
            _nominalLatitude = value;
        }
        public function get nominalLatitude():Number {
            return _nominalLatitude;
        }

        public function set nominalLatitudeAccuracy(value:Number):void {
            _nominalLatitudeAccuracy = value;
        }
        public function get nominalLatitudeAccuracy():Number {
            return _nominalLatitudeAccuracy;
        }

        public function set nominalLongitude(value:Number):void {
            _nominalLongitude = value;
        }
        public function get nominalLongitude():Number {
            return _nominalLongitude;
        }

        public function set nominalLongitudeAccuracy(value:Number):void {
            _nominalLongitudeAccuracy = value;
        }
        public function get nominalLongitudeAccuracy():Number {
            return _nominalLongitudeAccuracy;
        }

        public function set orientationDescription(value:String):void {
            _orientationDescription = value;
        }
        public function get orientationDescription():String {
            return _orientationDescription;
        }

        public function set outputs(value:ListCollectionView):void {
            _outputs = value;
        }
        public function get outputs():ListCollectionView {
            return _outputs;
        }

        public function set parentDataProducer(value:DataProducer):void {
            _parentDataProducer = value;
        }
        public function get parentDataProducer():DataProducer {
            return _parentDataProducer;
        }

        public function set person(value:Person):void {
            _person = value;
        }
        public function get person():Person {
            return _person;
        }

        public function set resources(value:ListCollectionView):void {
            _resources = value;
        }
        public function get resources():ListCollectionView {
            return _resources;
        }

        public function set role(value:String):void {
            _role = value;
        }
        public function get role():String {
            return _role;
        }

        public function set software(value:Software):void {
            _software = value;
        }
        public function get software():Software {
            return _software;
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

        public function set x3DOrientationText(value:String):void {
            _x3DOrientationText = value;
        }
        public function get x3DOrientationText():String {
            return _x3DOrientationText;
        }

        public function set xoffset(value:Number):void {
            _xoffset = value;
        }
        public function get xoffset():Number {
            return _xoffset;
        }

        public function set yoffset(value:Number):void {
            _yoffset = value;
        }
        public function get yoffset():Number {
            return _yoffset;
        }

        public function set zoffset(value:Number):void {
            _zoffset = value;
        }
        public function get zoffset():Number {
            return _zoffset;
        }

        public function get dateRange():IDateRange {
            return null;
        }

        public function readExternal(input:IDataInput):void {
            _childDataProducers = input.readObject() as ListCollectionView;
            _dataProducerGroups = input.readObject() as ListCollectionView;
            _dataProducerType = input.readObject() as String;
            _description = input.readObject() as String;
            _device = input.readObject() as Device;
            _endDate = input.readObject() as Date;
            _events = input.readObject() as ListCollectionView;
            _hostName = input.readObject() as String;
            _id = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _inputs = input.readObject() as ListCollectionView;
            _keywords = input.readObject() as ListCollectionView;
            _name = input.readObject() as String;
            _nominalBenthicAltitude = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalBenthicAltitudeAccuracy = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalDepth = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalDepthAccuracy = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalLatitude = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalLatitudeAccuracy = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalLongitude = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _nominalLongitudeAccuracy = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _orientationDescription = input.readObject() as String;
            _outputs = input.readObject() as ListCollectionView;
            _parentDataProducer = input.readObject() as DataProducer;
            _person = input.readObject() as Person;
            _resources = input.readObject() as ListCollectionView;
            _role = input.readObject() as String;
            _software = input.readObject() as Software;
            _startDate = input.readObject() as Date;
            _version = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _x3DOrientationText = input.readObject() as String;
            _xoffset = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _yoffset = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
            _zoffset = function(o:*):Number { return (o is Number ? o as Number : Number.NaN) } (input.readObject());
        }

        public function writeExternal(output:IDataOutput):void {
            output.writeObject(_childDataProducers);
            output.writeObject(_dataProducerGroups);
            output.writeObject(_dataProducerType);
            output.writeObject(_description);
            output.writeObject(_device);
            output.writeObject(_endDate);
            output.writeObject(_events);
            output.writeObject(_hostName);
            output.writeObject(_id);
            output.writeObject(_inputs);
            output.writeObject(_keywords);
            output.writeObject(_name);
            output.writeObject(_nominalBenthicAltitude);
            output.writeObject(_nominalBenthicAltitudeAccuracy);
            output.writeObject(_nominalDepth);
            output.writeObject(_nominalDepthAccuracy);
            output.writeObject(_nominalLatitude);
            output.writeObject(_nominalLatitudeAccuracy);
            output.writeObject(_nominalLongitude);
            output.writeObject(_nominalLongitudeAccuracy);
            output.writeObject(_orientationDescription);
            output.writeObject(_outputs);
            output.writeObject(_parentDataProducer);
            output.writeObject(_person);
            output.writeObject(_resources);
            output.writeObject(_role);
            output.writeObject(_software);
            output.writeObject(_startDate);
            output.writeObject(_version);
            output.writeObject(_x3DOrientationText);
            output.writeObject(_xoffset);
            output.writeObject(_yoffset);
            output.writeObject(_zoffset);
        }
    }
}