function ApplicationModel(map, cfg) {
    var self = this;


    self.map = map;
    self.username = "username here";
    //poly line reference
    self.trackLine = undefined;
    //marker array on poly line
    self.trackLineMarkers = [];
    //POI marker
    self.trackLineHIghlightMarker = undefined;
    //holds reference to speed/altitude chart
    self.speedChart;
    //holds reference to current bounds of markers
    self.bounds;
    self.topicItems = ko.observableArray([]);
    self.topicIds = ko.observable(-1);
    self.sessionItems = ko.observableArray([]);
    self.sessionIds = ko.observable(-1);
    self.currentTrackId = -1;

    //observable rate limit - only notify when this has stopped updating for at least 500ms
    self.trackData = ko.observableArray([]).extend({ rateLimit: { timeout: 500, method: "notifyWhenChangesStop" } });

    self.init = function() {
        //initialize chart
        self.speedChart = new google.visualization.ChartWrapper({
            chartType: 'LineChart',
            containerId: 'speedChart',
            options: {
                vAxes: [
                    {title: 'Speed km/h', titleTextStyle: {color: 'black'}, maxValue: 10}, // Left axis
                    {title: 'Alititude m', titleTextStyle: {color: 'black'}, maxValue: 20} // Right axis
                ],
                series: [
                    {targetAxisIndex: 0},
                    {targetAxisIndex: 1}
                ],
                hAxis: {
                    viewWindow: {
                        min: 0
                    }
                },

                tooltip: { trigger: 'selection' }
            }
        });

        //wait for ready event so that we can subscript to mouse over events
        google.visualization.events.addListener(self.speedChart, 'ready', function() {

            google.visualization.events.addListener(self.speedChart.getChart(), 'select', function() {
                var dt = self.speedChart.getDataTable(),
                    selection = self.speedChart.getChart().getSelection(),
                    row = selection.length ? selection[0].row : undefined;
                highlightMarker(self.trackLineHIghlightMarker,2);
                if (typeof(row) !== "undefined") {
                    self.map.panTo(self.trackLineMarkers[row].getPosition());
                    self.trackLineHIghlightMarker = highlightMarker(self.trackLineMarkers[row],4)
                }
            });

        });

        self.topicIds.subscribe(function(newvalue) {
            getSessions(newvalue);
        });
        self.sessionIds.subscribe(function(newvalue) {
            updateTrackData(newvalue);
        });
        //kick off dom updates
        self.trackData.subscribe(function(value) {
            drawTracks(value);
            drawSpeedChart(value);
        });

        //xhr list topics
        getTopics();
    } // end init

    self.refresh = function() {
        updateTrackData(self.currentTrackId);
    }

    self.deleteSelectedSession =  function() {
        console.log("I would delete session " + self.sessionIds());
        //deleteSession(self.sessionIds());
    }

    function deleteSession(sessionId) {
        $.ajax({
            url: '/api/sessions/delete/'+sessionId,
            dataType: 'json'
        })
            .done(function() {
                getTopics();
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function getSessions(topicId) {
        self.sessionItems.removeAll();
        $.ajax({
            url: '/api/sessions/'+topicId,
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.sessionItems.push({
                        text: new Date(item.date),
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function getTopics() {
        self.topicItems.removeAll();
        $.ajax({
            url: '/api/topics',
            dataType: 'json'
        })
            .done(function(data) {
                $.each(data, function(i, item) {
                    self.topicItems.push({
                        text: item.name,
                        id: item.id
                    });
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    function updateTrackData(trackId) {
        $.ajax({
            url: '/api/tracks/' + trackId,
            dataType: 'json'
        })
            .done(function(data) {
                self.currentTrackId = trackId;
                self.trackData.removeAll();
                $.each(data, function(i, item) {
                    self.trackData.push(item);
                });
            })
            .fail(function() {
                alert("failed.")
            });
    }

    //xhr to get paths for trackline, draw direction arrows, fit to bounds

    function drawTracks(tracks) {
        var path = [];
        bounds = new google.maps.LatLngBounds();
        removeTrackLine(self.trackLine);
        $.each(tracks, function(i, item) {
            var latLng = new google.maps.LatLng(item.lat,item.lon),
            marker = new MarkerWithLabel({
                position: latLng,
                icon: {
                    path: google.maps.SymbolPath.FORWARD_CLOSED_ARROW,
                    rotation: item.cog,
                    scale: 2,
                    strokeColor: 'green',
                    fillColor: 'green',
                    fillOpacity: 1
                },
                _index: i
            });
            google.maps.event.addListener(marker, 'mouseover', function() {
                var icon;
                //index is row number of dataTable for chart
                self.speedChart.getChart().setSelection([{row: this._index, column:1}]);
                //bounce the icon on mouse over
                highlightMarker(self.trackLineHIghlightMarker, 2);
                this.trackLineHIghlightMarker = highlightMarker(this, 4);
                //this.setAnimation(google.maps.Animation.BOUNCE);
            });
            google.maps.event.addListener(marker, 'mouseout', function() {
                var icon;
                //stop animation
                //this.setAnimation(null);
                highlightMarker(this,2, 'green');
            });
            self.trackLineMarkers.push(marker);
            path.push(latLng);
            bounds.extend(latLng);
        });
        drawTrackLine(path);
        //ensure map contains all markers
        self.map.fitBounds(bounds);
    }

    function highlightMarker(marker, scale) {
        if (marker) {
            var icon = marker.getIcon();
            icon.scale = scale;
            marker.setIcon(icon);
        }
        return marker;
    }

    //draw trackline on map
    function drawTrackLine(path) {
        self.trackLine  = new google.maps.Polyline({
            path: path,
            strokeColor: 'green',
            fillColor: 'green',
            fillOpacity: .7,
            strokeWeight: 3,
            strokeOpacity:.65
        });
        $.each(self.trackLineMarkers, function(i,item) {
           item.setMap(self.map);
        });
        self.trackLine.setMap(self.map);
    }

    //remove track line from map
    function removeTrackLine(path) {
        if (self.trackLine) {
            self.trackLine.setMap(null);
        }
        $.each(self.trackLineMarkers, function(i,item) {
           item.setMap(null);
        });
        self.trackLineMarkers = [];
    }

    /**
     * Draw a chart plotting speed, time, altitude
     * @param el
     * @param trackId
     */

    function drawSpeedChart(trackData) {
        var dataTable = new google.visualization.DataTable(),
            monthYearFormatter,
            startTime = 0;
        //TODO: fix this dependency
        //WARNING: dont forget to change columns indexes in hover over if you much wiht columns
        //tooltip must follow applicable column
        dataTable.addColumn('number', 'Date');
        dataTable.addColumn('number', 'Speed');
        dataTable.addColumn({type: 'string', role: 'tooltip'});
        dataTable.addColumn('number', 'Altitude');
        dataTable.addColumn({type: 'string', role: 'tooltip'});
        //add lat/lon for hover over event markers on map
        dataTable.addColumn('number', 'lat');
        dataTable.addColumn('number', 'lon');


        $.each(trackData, function (i, item) {
            if (i == 0) startTime = item.tst;
            var minutes = (item.tst - startTime) / 60;
            dataTable.addRow([ minutes , item.vel, getSpeedChartToolTip(item, {minutes: minutes}), item.alt, getSpeedChartToolTip(item, {minutes: minutes}), item.lat, item.lon])

        });

        self.speedChart.setDataTable(dataTable);
        self.speedChart.setView({
            columns: [0,1,2,3,4]
        });
        self.speedChart.draw();
    }

    //TODO: replace with HTML tooltip
    function getSpeedChartToolTip(item,extra) {
        return "Date: " + new Date(item.tst*1000) + "\n" +
                "Time: " + Math.floor(extra.minutes) + "\n" +
                "Velocity: " + item.vel + "km/h (" + Math.floor(item.vel * .621371) + " mph)\n" +
                "Altitude: " + item.alt + "m (" + Math.floor(item.alt * 3.28084) +  "ft)";
    }

}