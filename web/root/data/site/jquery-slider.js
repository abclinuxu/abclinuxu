/**
 * Slides content of container down at given speed by diff pixels to maximal
 * value to maxOffset. When mouse is moved over container, sliding is stopped
 * and when mouse leaves container, its content is moved upwards in a faster
 * way.
 * 
 * @param container Container which inner content is slided down
 * @param speed Animation timeout
 * @param diff Number of functions moved
 * 
 */
function Slider(container, speed, diff, maxOffset) {
	this.container = container;
	this.speed = speed || 1000;
	this.diff = diff || 1;
	this.maxOffset = maxOffset || 350;
	this.offset = 0;

	var ref = this;
	container.bind('mouseenter', function(e) {
		Slider.prototype.stop.call(ref);
		return false;
	});

	container.bind('mouseleave', function(e) {
		Slider.prototype.restart.call(ref);
		return false;
	});
	
	// scroll back when page is moved
	$(window).unload(function() {
		container.scrollTop('0px');
	});
	// start execution
	Slider.prototype.start.call(ref);	
	
};

Slider.prototype.animate = function() {
	var ref = this;
	if ((this.offset + this.diff) < this.maxOffset) {
		var sDiff = '+=' + this.diff + 'px';
		this.offset += this.diff;
		this.container.animate( {
			scrollTop :sDiff
		}, this.speed * 2, function() {
			Slider.prototype.animate.call(ref);
		});
	}
};

Slider.prototype.start = function() {
	this.animate();
};

Slider.prototype.restart = function() {
	var ref = this;
	var sDiff = '-=' + this.offset + 'px';
	this.offset = 0;
	this.container.animate( {
		scrollTop :sDiff
	}, this.speed, function() {
		Slider.prototype.start.call(ref);
	});
};

Slider.prototype.stop = function() {
	this.container.stop(true, false);
};