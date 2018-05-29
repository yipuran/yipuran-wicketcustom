/*
 * progressmodal.js
 *
 * initProgressModal( selector
 *                  ,{  message: "Loading..."
 *                    , width: "220px"
 *                    , height: "100px"
 *                    , fontsize: "20px"
 *                    , fontfamily: "Century"
 *                    , spinopts : {   // spin option ==> http://fgnass.github.io/spin.js
 *                                   lines: 12,            // The number of lines to draw
 *                                   length: 7,            // The length of each line
 *                                   width: 5,             // The line thickness
 *                                   radius: 10,           // The radius of the inner circle
 *                                   rotate: 0,            // Rotation offset
 *                                   corners: 1,           // Roundness (0..1)
 *                                   color: '#000000',     // #rgb or #rrggbb
 *                                   direction: 1,         // 1: clockwise, -1: counterclockwise
 *                                   speed: 1,             // Rounds per second
 *                                   trail: 100,           // Afterglow percentage
 *                                   opacity: 1/4,         // Opacity of the lines
 *                                   fps: 20,              // Frames per second when using setTimeout()
 *                                   zIndex: 2e9,          // Use a high z-index by default
 *                                   className: 'spinner', // CSS class to assign to the element
 *                                   top: 'auto',          // center vertically
 *                                   left: 'auto',         // center horizontally
 *                                   position: 'relative'  // element position
 *                                 }
 *                   }
 *    );
 *  displayProgressModal("div#modal", true);
 *
 */
var _progressmodal_spinner, body_margin, body_padding;
function initProgressModal(selector, options){
	var container = selector + " div.progressmodal-container";

	$(selector).append('<div class="progressmodal-background"></div>');
	$(selector).append('<div class="progressmodal-container"></div>');
	var spinopts = { top: 'auto' };
	if ($.isEmptyObject(options)){
		$(container).append('<table><tr><td class="progressmodal-spin"><div id="progressmodal-spin"></div></td><td>Loading...</td></tr></table>');
		// CSS 初期化
		initProgressModalCSS(selector);
	}else{
		if ($.isEmptyObject(options['message'])){
			$(container).append('<table><tr><td class="progressmodal-spin"><div id="progressmodal-spin"></div></td><td>Loading...</td></tr></table>');
		}else{
			$(container).append('<table><tr><td class="progressmodal-spin"><div id="progressmodal-spin"></div></td></tr><tr><td>'+options['message']+'</td></tr></table>');
		}
		// CSS 初期化
		initProgressModalCSS(selector);
		if (!$.isEmptyObject(options['width'])){
			$(container).width(options['width']);
		}
		if (!$.isEmptyObject(options['height'])){
			$(container).height(options['height']);
		}
		if (!$.isEmptyObject(options['fontsize'])){
			$(container+" table td").css('font-size', options['fontsize']);
		}
		if (!$.isEmptyObject(options['fontfamily'])){
			$(container+" table td").css('font-family', options['fontfamily']);
		}
		if (!$.isEmptyObject(options['spinopts'])){
			spinopts = options['spinopts'];
		}
	}
	_progressmodal_spinner = new Spinner(spinopts);
	adjustProgressModalCenter(container);
	$(window).resize(function(){
		adjustProgressModalCenter(container);
	});
	body_margin = $('body').css("margin");
	body_padding = $('body').css("padding");
}
function initProgressModalCSS(selector){
	var divModalObj = new Object();
	divModalObj.display = "none!important";
	divModalObj.position = "fixed!important";
	divModalObj.width = "100%!important";
	divModalObj.height = "100%!important";
	$(selector).css(divModalObj);
}
// プログレスモーダル open/close
function displayProgressModal(selctor, toOpen){
	if (toOpen){
		$(selctor).fadeIn(500);
		_progressmodal_spinner.spin($('#progressmodal-spin').get(0));
		$('body').css("margin", "0");
		$('body').css("padding", "0");
	}else{
		_progressmodal_spinner.stop();
		$(selctor).fadeOut(250);
		$('body').css("margin", body_margin);
		$('body').css("padding", body_padding);
		$(selctor).empty();
	}
}
//ウィンドウの位置をセンターに調整
function adjustProgressModalCenter(target){
	var w = $(target).parent().parent().width();
	var h = $(target).parent().parent().height();
	var margin_top = (h - $(target).height()) / 2 ;
	if (margin_top < 1){
		margin_top = 100;
	}
	var margin_left = (w - $(target).width()) / 2;
	$(target).css({top:margin_top+"px", left:margin_left+"px"});
	$('#progress').css("width", w+"px");
	$('#progress').css("height", h+"px");
}

