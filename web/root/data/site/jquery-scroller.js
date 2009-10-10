/**
 * Slides content of container down at given speed by diff pixels to maximal
 * value to maxOffset. When mouse is moved over container, sliding is stopped
 * and when mouse leaves container, its content is moved upwards in a faster
 * way.
 * 
 * @param container Container which inner content is slided down and up
 * @param settings Settings where defaults are not appropriate  
 * 
 */
var ScrollerDefaults = {
		speed: 6000,
		waitTime: 1000,
		initTime: 10000,
		offset: 350,
		goingUp: false
};

function Scroller(container, settings) {
	this.container = container;
	this.settings = $.extend( {}, ScrollerDefaults, settings );	
	
	var ref = this;
	container.bind('mouseenter', function(e) {
		Scroller.prototype.stop.call(ref);
		return false;
	});

	container.bind('mouseleave', function(e) {
		Scroller.prototype.restart.call(ref);
		return false;
	});

	// scroll back when page is moved
	$(window).unload( function() {
		container.scrollTop('0px');
	});	
	Scroller.prototype.start.call(this);
};

Scroller.prototype.animate = function() {
	Scroller.prototype.wait.call(this, this.settings.waitTime);
	var sDiff = (this.settings.goingUp == true) ? '0px' : this.settings.offset + 'px';
	this.container.animate( {
		scrollTop :sDiff
	}, this.settings.speed);
	this.settings.goingUp = !this.settings.goingUp;

};

Scroller.prototype.wait = function(waitTime) {
	this.container.animate( {
		opacity :'+=0'
	}, waitTime);
}

Scroller.prototype.start = function() {
	Scroller.prototype.wait.call(this, this.settings.initTime);
	this.animate();
};

Scroller.prototype.restart = function() {
	this.animate();
};

Scroller.prototype.stop = function() {
	this.container.stop(true, false);
};