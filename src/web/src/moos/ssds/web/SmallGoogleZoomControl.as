package moos.ssds.web
{
	import com.google.maps.controls.ControlBase;
	import com.google.maps.controls.ControlPosition;
	import com.google.maps.interfaces.IMap;
	
	import flash.display.Shape;
	import flash.display.Sprite;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.text.TextField;
	import flash.text.TextFieldAutoSize;
	import flash.text.TextFormat;

	public class SmallGoogleZoomControl extends ControlBase
	{
		public function SmallGoogleZoomControl()
		{
			// Control will be placed at the top left corner of the map,
			// 7 pixels from the edges.
			super(new ControlPosition(ControlPosition.ANCHOR_TOP_LEFT, 5, 5));
		}
		
		public override function initControlWithMap(map:IMap):void {
			// first call the base class
			super.initControlWithMap(map);
			createButton("+", 0, 0, function(event:Event):void { map.zoomIn(); });
			createButton("-", 0, 20, function(event:Event):void { map.zoomOut(); });
  		}
        
		private function createButton(text:String,x:Number,y:Number,callback:Function):void {
			var button:Sprite = new Sprite();
			button.x = x;
			button.y = y;
                
			var label:TextField = new TextField();
			label.text = text;
			label.x = 2;
			label.selectable = false;
//			label.autoSize = TextFieldAutoSize.CENTER;
			var format:TextFormat = new TextFormat("Verdana");
			label.setTextFormat(format);
      
			var buttonWidth:Number = 20;
			var background:Shape = new Shape();
			background.graphics.beginFill(0xFFFFFF);
			background.graphics.lineStyle(1, 0x000000);
			background.graphics.drawRoundRect(0, 0, buttonWidth, 20, 4);
			background.graphics.endFill();

			button.addChild(background);
			button.addChild(label);
			button.addEventListener(MouseEvent.CLICK, callback);
                
			addChild(button);
		}
	}
}