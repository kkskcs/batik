var svgns   = "http://www.w3.org/2000/svg";
var xlinkns = "http://www.w3.org/1999/xlink";

function Pile(board, x, y, w, h, dx, dy, pdx, pdy) {
  this.board = board;
  this.doc   = board.doc;
  this.index = board.thePiles.length;

  board.thePiles.push(this);
  this.g = this.doc.createElementNS(svgns, "g");
  this.g.setAttribute("id", "pile-"+this.index);
  this.g.addEventListener("mousedown", new PileMouseDownHandler(this), false);

  this.base = this.doc.createElementNS(svgns, "rect");
  this.base.setAttribute("x", ""+x);
  this.base.setAttribute("y", ""+y);
  this.base.setAttribute("width", ""+w);
  this.base.setAttribute("height", ""+h);
  this.base.setAttribute("style", "visibility:hidden; pointer-events:fill");
  this.g.appendChild(this.base);

  board.boardGroup.appendChild(this.g);
  this.x = x;
  this.y = y;
  this.width = w;
  this.height = h;
  this.dx = dx;
  this.dy = dy;
  this.pdx = pdx;
  this.pdy = pdy;
  this.cards = new Array();
  var me = this;
}

Pile.prototype.addCard = function(card) {
  if (card.pile) {
    card.pile.removeCard(card);
  }
  var len = this.cards.length;
  if ((this.pdx != 0) || (this.pdy !=0)) {
    for (var i=0; i<len; i++) {
      this.cards[i].setPos(this.x+this.pdx*len+this.dx*i,
                           this.y+this.pdy*len+this.dy*i);
    }
  }
  card.pile = this;
  this.cards[len] = card;
  card.rect.style.setProperty("pointer-events", "fill", "");
  card.setPos(this.x+this.pdx*len+this.dx*len,
              this.y+this.pdy*len+this.dy*len);
  this.g.appendChild(card.getElem());
}

Pile.prototype.removeCard = function(card) {
  if (this != card.pile) return;
  var len = this.cards.length;
  for (var i=len-1; i>=0; i--) {
    var c = this.cards[i];
    if (c == card) {
      this.cards.splice(i, 1);
      len = this.cards.length;
      for (var j=i; j<len; j++) {
        c = this.cards[j];
        c.setPos(this.x+this.dx*j, this.y+this.dy*j);
      }
      card.pile = null;
      break;
    }
  }
  if ((this.pdx != 0) || (this.pdy !=0)) {
    var len = this.cards.length;
    var x = this.x+this.pdx*(len-1);
    var y = this.y+this.pdy*(len-1);
    for (var i=0; i<len; i++) {
      this.cards[i].setPos(x+this.dx*i, y+this.dy*i);
    }
  }
}

Pile.prototype.moveCardTo = function(card, step) {
  if (card.pile) {
    card.pile.removeCard(card);
  }
  var len = this.cards.length;
  card.pile = this;
  this.cards.push(card);
  card.rect.style.setProperty("pointer-events", "fill", "");
  this.board.moveGroup.appendChild(card.getElem());
  var x = this.x+this.pdx*len;
  var y = this.y+this.pdy*len;
  card.moveTo(x+this.dx*len, y+this.dy*len, step);
}

Pile.prototype.moveCardsTo = function(cards, step) {
  var xlocs = new Array();
  var ylocs = new Array();
  for (var i=0; i<cards.length; i++) {
    xlocs[i] = cards[i].x;
    ylocs[i] = cards[i].y;
  }

  for (var i=0; i<cards.length; i++) {
    var card = cards[i];
    if (card.pile) {
      card.pile.removeCard(card);
    }
    card.setPos(xlocs[i], ylocs[i]);
    var len = this.cards.length;
    card.pile = this;
    this.cards.push(card);
    card.rect.style.setProperty("pointer-events", "fill", "");
    this.board.moveGroup.appendChild(card.getElem());
    var x = this.x+this.pdx*len;
    var y = this.y+this.pdy*len;
    card.moveTo(x+this.dx*len, y+this.dy*len, step);
    this.moveCardTo(card, step);
  }
}

function moveCardsToPiles(cards, piles, step) {
  var xlocs = new Array();
  var ylocs = new Array();
  for (var i=0; i<cards.length; i++) {
    xlocs[i] = cards[i].x;
    ylocs[i] = cards[i].y;
  }
  
  for (var i=0; i<cards.length; i++) {
    var card = cards[i];
    var pile = piles[i];
    if (card.pile) {
      card.pile.removeCard(card);
    }
    card.setPos(xlocs[i], ylocs[i]);
    var len = pile.cards.length;
    card.pile = pile;
    pile.cards.push(card);
    card.rect.style.setProperty("pointer-events", "fill", "");
    pile.board.moveGroup.appendChild(card.getElem());
    var x = pile.x+pile.pdx*len;
    var y = pile.y+pile.pdy*len;
    card.moveTo(x+pile.dx*len, y+pile.dy*len, step);
    pile.moveCardTo(card, step);
  }
}



Pile.prototype.size = function() {
  return this.cards.length;
}

Pile.prototype.getTopCard = function() {
  if (this.cards.length == 0) return null;
  var c = this.cards.pop();
  c.pile = null;
  if ((this.pdx != 0) || (this.pdy !=0)) {
    var len = this.cards.length;
    var x = this.x+this.pdx*(len-1);
    var y = this.y+this.pdy*(len-1);
    for (var i=0; i<len; i++) {
      this.cards[i].setPos(x+this.dx*i, y+this.dy*i);
    }
  }
  return c;
}

Pile.prototype.checkTopCard = function() {
  if (this.cards.length == 0) return null;
  return this.cards[this.cards.length-1];
}

Pile.prototype.checkCard = function(index) {
  return this.cards[index];
}

Pile.prototype.nextCard = function(card) {
  var sz = this.cards.length-1;
  for (var i=0; i<sz; i++) {
    if (this.cards[i] == card)
      return this.cards[i+1];
  }
  return null;
}

Pile.prototype.setDragCheck = function(dragCheck) {
  this.dragCheck = dragCheck;
}

Pile.prototype.setDropCheck = function(dropCheck) {
  this.dropCheck = dropCheck;
}

function PileMouseDownHandler(pile) {
  this.pile = pile;
  this.handleEvent = function(evt) {
    if (!pile.dragCheck) return;
    var tgt = evt.target;
    var e = tgt;
    var p = e.parentNode;
    while (p && (p != pile.g)) {
      e = p;
      p = e.parentNode;
    }

    if (!p) return;
    var c, i;
    var num = pile.cards.length;
    for (i=num-1; i>=0; i--) {
      c = pile.cards[i];
      if (e == c.elem)
        break;
      c = null;
    }
    if (!c) return;
    
    var g = pile.dragCheck(pile, c, i);
    if (!g) return;
    var cards = pile.cards.splice(i, num-i);

    new CardMoveManager(g, this.pile, cards, 
                        evt.clientX, evt.clientY);
  }
}
/**
 * Finds the pile associated with elem, if any.
 * returns null if elem is not associated with a pile.
 * otherwise it returns the associated Pile object.
 */
function findPile(board, elem) {
  var root = document.getRootElement();
  var tgt = elem
  var destPile = null;
  while (tgt != root) {
    var id;
    try { id = tgt.getAttribute("id"); } catch (x) { break; }
    if (id && id.substring(0, 5) == "pile-") {
      var idx = parseFloat(id.substring(5));
      destPile = board.thePiles[idx];
      break;
    }
    tgt = tgt.parentNode;
  }
  return destPile;
}

function CardMoveManager(group, pile, cards, x, y) {
  this.pile = pile;
  this.board = pile.board;
  this.cards = cards;
  this.group = group;
  this.parent = group.parentNode;
  this.root = this.group.getOwnerDocument().getRootElement();
  this.startPt = localPt(this.parent, x, y);
  this.fmm = new ForwardMouseMove(this);
  this.fmu = new ForwardMouseUp(this);
  this.root.addEventListener("mousemove", this.fmm, false);
  this.root.addEventListener("mouseup", this.fmu, false);
  this.moved = false;

  this.mousemove = function(evt) {
    var pt = localPt(this.parent, evt.clientX, evt.clientY);
    var dx = pt.x-this.startPt.x;
    var dy = pt.y-this.startPt.y;
    if (!this.moved && ((dx*dx+dy*dy) < 4))
      return;

    if (!this.moved) {
      this.moved = true;
      // Now move cards 
      for (var i=0; i<this.cards.length; i++) {
        this.cards[i].pile = null;
        this.cards[i].rect.style.removeProperty("pointer-events");
        this.group.appendChild(this.cards[i].elem);
      }
    }
        
    this.group.setAttribute("transform", "translate(" + dx + ", " + dy + ")");
  };

  this.mouseup = function(evt) {
    this.root.removeEventListener("mousemove", this.fmm, false);
    this.root.removeEventListener("mouseup", this.fmu, false);
    this.group.setAttribute("transform","");
    if (!this.moved) {
      for (var i=0; i<this.cards.length; i++) {
        this.pile.cards.push(this.cards[i]);
      }
      return;
    }

    var pt = localPt(this.parent, evt.clientX, evt.clientY);
    var dx = pt.x-this.startPt.x;
    var dy = pt.y-this.startPt.y;

    var moveInfo = false;
    var destPile = findPile(this.board, evt.target);
    if (destPile) {
      moveInfo = destPile.dropCheck(this.pile, destPile, this.cards);
    }

    switch(moveInfo) {
    case true:
      // Drop succeeded but no move info.
      this.pile.board.notifyMoveDone();
      return;
    case false:
      // Drop failed move back to original.
      for (var i=0; i<this.cards.length; i++) {
        var c = this.cards[i];
        c.setPos(c.x+dx, c.y+dy);
      }
      this.pile.moveCardsTo(cards, 40);
      return;
    default:
      // Drop succeed record move info.
      this.pile.board.saveMove(moveInfo);
      this.pile.board.notifyMoveDone();
      return;
    }
  }
}
