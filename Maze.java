import java.util.*;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;


// to represent a Vertex
class Vertex {
  int x;
  int y;
  Color color;
  ArrayList<Edge> outEdges;

  Vertex(int x, int y) {
    this.x = x;
    this.y = y;
    this.color = new Color(255, 255, 255);
    this.outEdges = new ArrayList<>();
  }
  
  Vertex(int x, int y, Color color) {
    this.x = x;
    this.y = y;
    this.color = color;
    this.outEdges = new ArrayList<>();
  }

  Vertex(int x, int y, ArrayList<Edge> outEdges) {
    this.x = x;
    this.y = y;
    this.color = new Color(255, 255, 255);
    this.outEdges = outEdges;
  }
  
  // adds edge to outedges
  void connect(Edge e) {
    this.outEdges.add(e);
  }

  // determines if this Vertex has a path to the given Vertex
  boolean hasPathTo(Vertex b) {
    for (Edge e : this.outEdges) {
      if (e.to.equals(b) || e.to.hasPathTo(b)) {
        return true;
      }
    }
    return false;
  }
  
  // determines if this Vertex has a path to the given Vertex
  boolean hasPathBetween(Vertex to) {
    Deque<Vertex> alreadySeen = new ArrayDeque<Vertex>();
    Deque<Vertex> worklist = new ArrayDeque<Vertex>();
    
    worklist.add(this);
    
    while (!worklist.isEmpty()) {
      Vertex next = worklist.remove();
      
      if (next.equals(to)) {
        return true;
        
      } else if (!alreadySeen.contains(next)) {
        
        for (Edge e : next.outEdges) {
          worklist.add(e.to);
        }
        
        alreadySeen.add(next);
      }
    }
    return false;
  }

  // checks if this Vertex equals the given Vertex
  public boolean equals(Object b) {
    if (b instanceof Vertex) {
      Vertex other = (Vertex) b;
      return this.x == other.x && this.y == other.y;
    }
    else {
      return false;
    }
  }
  
  //overrides the hashCode function
  public int hashCode() {
    return this.x * 11 + this.y * 7;
  }

  // draws this Vertex
  public WorldImage drawVertex() {
    return new RectangleImage(Maze.SCALE_WIDTH - 3, Maze.SCALE_HEIGHT - 3, "solid",
        this.color);
  }


}

// to represent an Edge
class Edge {
  Vertex from;
  Vertex to;
  int weight;

  Edge(Vertex from, Vertex to) {
    this.from = from;
    this.to = to;
  }

  Edge(Vertex from, Vertex to, int weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  // checks if this Edge equals the given Edge
  public boolean equals(Object e) {
    if (e instanceof Edge) {
      Edge other = (Edge) e;
      return this.from.equals(other.from) && this.to.equals(other.to)
          && this.weight == other.weight;
    }
    else {
      return false;
    }
  }
  
  //overrides the hashcode method to check for equality
  public int hashCode() {
    return this.from.hashCode() + this.to.hashCode() + this.weight * 11;
  }
  


  //draws a Vertical Edge
  public WorldImage drawVerticalEdge() {
    return new RectangleImage(3, Maze.SCALE_HEIGHT, "solid", new Color(0, 0, 0));
  }

  // draws a Horizontal Edge
  public WorldImage drawHorizontalEdge() {
    return new RectangleImage(Maze.SCALE_WIDTH, 3, "solid", new Color(0, 0, 0));
  }

  // draws a Vertical Edge
  public WorldImage drawVerticalBranch() {
    return new RectangleImage(3, Maze.SCALE_HEIGHT, "solid", this.from.color);
  }

  // draws a Horizontal Edge
  public WorldImage drawHorizontalBranch() {
    return new RectangleImage(Maze.SCALE_WIDTH, 3, "solid", this.from.color);
  }
}

// to represent the Forbidden Island world
class Maze extends World {

  // Defines an int constant
  static int MAZE_HEIGHT = 15;

  // Defines an int constant
  static int MAZE_WIDTH = 20;

  // scale for the vertex size
  static int SCALE_HEIGHT = 600 / MAZE_HEIGHT;
  
  static int SCALE_WIDTH = 1000 / MAZE_WIDTH;

  // random nummber generator
  Random rand = new Random();

  // sets the random number weight for the y edges
  int yNum = 100;

  // sets the random number weight for the x edges
  int xNum = 100;

  // represents the initial vertices
  ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>(MAZE_WIDTH);

  // represents the vertices
  ArrayList<Vertex> vert = new ArrayList<Vertex>(MAZE_WIDTH * MAZE_HEIGHT);

  // to represent the tree
  HashMap<Vertex, Vertex> rep = new HashMap<Vertex, Vertex>();

  // to represent the edges in the tree
  ArrayList<Edge> edges = new ArrayList<Edge>();

  // to represent the edges sorted by weights
  ArrayList<Edge> workList = new ArrayList<Edge>();
  
  // to represent a stack or queue
  Deque<Vertex> deque = new ArrayDeque<Vertex>();
  
  // to represent the edges in the graph used to get to the current vertex
  HashMap<Vertex, Edge>  cameFromEdge = new HashMap<Vertex, Edge>();
  
  // to represent the path of the solved maze
  LinkedList<Vertex> path = new LinkedList<Vertex>();
  
  //to represent all vertex cells visited by search
  LinkedList<Vertex> visited = new LinkedList<Vertex>();

  // to represent the edges that compose the maze
  ArrayList<Edge> maze = new ArrayList<Edge>();
  
  // to represent wrong moves score
  Integer wrongmoves;
  
  // to represent whether the search is active
  boolean active;
  
  // to represent whether the graph is solved
  boolean solved;
  

  Maze() {
    this.active = false;
    this.solved = false;
    this.wrongmoves = 0;
    this.initVertex();
    this.initEdges();
    this.initWorkList();
    this.initRep();
    this.build();
    this.initMaze();
    this.makeScene();
  }
  
  Maze(int col, int row) {
    MAZE_HEIGHT = row;
    MAZE_WIDTH = col;
    SCALE_HEIGHT = 600 / row;
    SCALE_WIDTH = 1000 / col;
    this.wrongmoves = 0;
    this.active = false;
    this.solved = false;
    this.initVertex();
    this.initEdges();
    this.initWorkList();
    this.initRep();
    this.build();
    this.initMaze();
    this.makeScene();
  }

  // to initialize the vertices
  void initVertex() {
    for (int i = 0; i < MAZE_HEIGHT; i++) {
      ArrayList<Vertex> alv = new ArrayList<Vertex>(MAZE_HEIGHT);
      for (int j = 0; j < MAZE_WIDTH; j++) {
        if (i == 0 && j == 0) {
          alv.add(j, new Vertex(j, i, new Color(128, 206, 225)));
        } else if (j == MAZE_WIDTH - 1 && i == MAZE_HEIGHT - 1) {
          alv.add(j, new Vertex(j, i, new Color(177, 156, 217)));
        } else {
          alv.add(j, new Vertex(j, i));
        }
      }
      this.vertices.add(i, alv);
    }

    for (int a = 0; a < MAZE_HEIGHT; a++) {
      for (int b = 0; b < MAZE_WIDTH; b++) {
        this.vert.add(this.vertices.get(a).get(b));
      }
    }
  }

  // to initialize the vertices with Edges
  void initEdges() {
    for (int i = 0; i < MAZE_HEIGHT; i++) {
      for (int j = 0; j < MAZE_WIDTH; j++) {
        this.createVertex(i, j);
      }
    }
  }

  // to initialize the vertices with Edges
  void createVertex(int i, int j) {
    Vertex v = this.vertices.get(i).get(j);
    if (j != 0 && (j % 2 == 0)) {
      int randomNum = rand.nextInt(this.yNum);
      this.workList.add(new Edge(v, this.vertices.get(i).get(j - 1), randomNum));
    }
    
    if (j != MAZE_WIDTH - 1 && (j % 2 == 0)) {
      int randomNum = rand.nextInt(this.yNum);
      this.workList.add(new Edge(v, this.vertices.get(i).get(j + 1), randomNum));
    }
    
    if (i != 0 && (i % 2 == 0)) {
      int randomNum = rand.nextInt(this.xNum);
      this.workList.add(new Edge(v, this.vertices.get(i - 1).get(j), randomNum));
    }
    
    if (i != MAZE_HEIGHT - 1 && (i % 2 == 0)) {
      int randomNum = rand.nextInt(this.xNum);
      this.workList.add(new Edge(v, this.vertices.get(i + 1).get(j), randomNum));
    }
  }

  // sorts this workList
  void initWorkList() {
    Collections.sort(this.workList, new Comparator<Edge>() {
      public int compare(Edge e1, Edge e2) {
        return e1.weight - e2.weight;
      }
    });
  }
  

  // initializes the HashMap
  void initRep() {
    for (Vertex v : this.vert) {
      this.rep.put(v, v);
    }
  }

  // to initialize the graph
  void build() {
    ArrayList<Edge> workListCopy = new ArrayList<Edge>(this.workList);
    
    while (this.edges.size() < this.vert.size() - 1) {
      Edge e = workListCopy.get(0);
      Vertex x = e.from;
      Vertex y = e.to;
      workListCopy.remove(0);
      if (!(this.find(x).equals(this.find(y)))) {
        this.edges.add(e);
        this.union(x, y);
        Vertex vertexFrom = this.vertices.get(x.y).get(x.x);
        Vertex vertexTo = this.vertices.get(y.y).get(y.x);
        vertexFrom.outEdges.add(new Edge(vertexFrom, vertexTo, e.weight));
        vertexTo.outEdges.add(new Edge(vertexTo, vertexFrom, e.weight));
      }
    }
  }

  // assigns the representative of a to the representative of b
  void union(Vertex a, Vertex b) {
    this.rep.put(this.find(a), this.find(b));
  }

  // finds the representative of the given Vertex
  Vertex find(Vertex a) {
    if (this.rep.get(a).equals(a)) {
      return a;
    }
    else {
      return this.find(this.rep.get(a));
    }
  }
  
  // to creates a solution path to maze using breath first search 
  void searchBFS(Vertex start, Vertex target) {
    this.deque.addLast(start);
    this.cameFromEdge.put(start, null);
    
    while (!this.deque.isEmpty()) {
      Vertex next = this.deque.removeFirst();
      this.visited.add(next);
      
      if (next.equals(target)) {
        initPath();
        break;
      } 
      
      for (Edge e: next.outEdges) {
        if (!this.cameFromEdge.containsKey(e.to)) {
          this.deque.addLast(e.to);
          this.cameFromEdge.put(e.to, e);
        } else {
          this.wrongmoves++;
        }
      }
    }  
  }
  
  
  // to creates a solution path to maze using depth first search 
  void searchDFS(Vertex start, Vertex target) {
    this.deque.addFirst(start);
    this.cameFromEdge.put(start, null);
    
    while (!this.deque.isEmpty()) {
      Vertex next = this.deque.removeFirst();
      
      if (next.equals(target)) {
        initPath();
        break;
      } 
      
      for (Edge e: next.outEdges) {
        if (!this.cameFromEdge.containsKey(e.to)) {
          this.deque.addFirst(e.to);
          this.visited.add(next);
          this.cameFromEdge.put(e.to, e);
        } else {
          this.wrongmoves++;
        }
      }
    }
  }
  
  // to construct the list of vertex on the path
  void initPath() {
    Vertex current = this.vert.get(MAZE_WIDTH * MAZE_HEIGHT - 1);
    this.path.addFirst(current);
    Edge next = this.cameFromEdge.get(current);
    
    while (next != null) {
      current = next.from;
      next = this.cameFromEdge.get(current);
      path.addFirst(current);
    }
  }
  
  // to construct the List of Edges in the Maze
  void initMaze() {
    for (Edge e : this.workList) {
      if (!(this.edges.contains(e))) {
        this.maze.add(new Edge(e.from, e.to));
      }
    }
  }


  // produces the image of the world
  public WorldScene makeScene() {
    WorldScene s = new WorldScene(1000, 700);
    WorldImage boarderY = new RectangleImage(3, MAZE_HEIGHT * SCALE_HEIGHT, 
        "solid", new Color(0, 0, 0));
    WorldImage boarderX = new RectangleImage(MAZE_WIDTH * SCALE_WIDTH, 3, 
        "solid", new Color(0, 0, 0));
    WorldImage score = new TextImage("Wrong Moves In Search: "  
        + this.wrongmoves.toString(), 24,FontStyle.BOLD,Color.BLACK);
    
    // walls of maze
    for (Edge e : this.workList) {
      if (!(this.edges.contains(e))) {
        if (e.to.x == e.from.x) {
          s.placeImageXY(e.drawHorizontalEdge(),
              (int)(((e.to.x + e.from.x) / 2.0) * SCALE_WIDTH) + SCALE_WIDTH / 2,
              (int)(((e.to.y + e.from.y) / 2.0) * SCALE_HEIGHT) + SCALE_HEIGHT / 2);
        } else if (e.to.y == e.from.y) {
          s.placeImageXY(e.drawVerticalEdge(),
              (int)(((e.to.x + e.from.x) / 2.0) * SCALE_WIDTH) + SCALE_WIDTH / 2,
              (int)(((e.to.y + e.from.y) / 2.0) * SCALE_HEIGHT) + SCALE_HEIGHT / 2);
        }
      } else {
        if (e.to.x == e.from.x) {
          s.placeImageXY(e.drawHorizontalBranch(),
              (int)(((e.to.x + e.from.x) / 2.0) * SCALE_WIDTH) + SCALE_WIDTH / 2,
              (int)(((e.to.y + e.from.y) / 2.0) * SCALE_HEIGHT) + SCALE_HEIGHT / 2);
        } else if (e.to.y == e.from.y) {
          s.placeImageXY(e.drawVerticalBranch(),
              (int)(((e.to.x + e.from.x) / 2.0) * SCALE_WIDTH) + SCALE_WIDTH / 2,
              (int)(((e.to.y + e.from.y) / 2.0) * SCALE_HEIGHT) + SCALE_HEIGHT / 2);
        }
      }
    }
    
    // cells of maze
    for (Vertex v : this.vert) {
      s.placeImageXY(v.drawVertex(), (v.x * SCALE_WIDTH) + (SCALE_WIDTH / 2), 
          (v.y * SCALE_HEIGHT) + (SCALE_HEIGHT / 2));
    }
    
    // boarders of maze
    s.placeImageXY(boarderY, 1, (MAZE_HEIGHT * SCALE_HEIGHT) / 2);
    s.placeImageXY(boarderY, (MAZE_WIDTH * SCALE_WIDTH) - 1, (MAZE_HEIGHT * SCALE_HEIGHT) / 2);
    s.placeImageXY(boarderX, (MAZE_WIDTH * SCALE_WIDTH) / 2, 1);
    s.placeImageXY(boarderX, (MAZE_WIDTH * SCALE_WIDTH) / 2, (MAZE_HEIGHT * SCALE_HEIGHT) - 1);
    
    // Score
    s.placeImageXY(score, 500, 675);
    
    return s;
  }
  
  // key event for the the game: if r is prested the game will reset
  public void onKeyEvent(String key) {
    if (key.equals("r")) {
      this.rand = new Random();
      this.yNum = 100;
      this.xNum = 100;
      this.vertices = new ArrayList<ArrayList<Vertex>>(MAZE_WIDTH);
      this.vert = new ArrayList<Vertex>(MAZE_WIDTH * MAZE_HEIGHT);
      this.rep = new HashMap<Vertex, Vertex>();
      this.edges = new ArrayList<Edge>();
      this.workList = new ArrayList<Edge>();
      this.maze = new ArrayList<Edge>();
      this.cameFromEdge = new HashMap<Vertex, Edge>();
      this.path = new LinkedList<Vertex>();
      this.deque = new ArrayDeque<Vertex>();
      this.wrongmoves = 0;
      this.initVertex();
      this.initEdges();
      this.initWorkList();
      this.initRep();
      this.build();
      this.initMaze();
      this.makeScene();
    }
    
    // bfs search
    if (key.equals("b")) { 
      this.searchBFS(this.vertices.get(0).get(0),
          this.vertices.get(MAZE_HEIGHT - 1).get(MAZE_WIDTH - 1));
      this.active = true;
    }
    
    // dfs search
    if (key.equals("d")) {
      this.searchDFS(this.vertices.get(0).get(0),
          this.vertices.get(MAZE_HEIGHT - 1).get(MAZE_WIDTH - 1));
      this.active = true;
    }
  }
  
  // on every tick, updates the current state of the graph to show progress in solving
  public void onTick() {
    if (this.active) {
      if (!this.visited.isEmpty()) {
        this.visited.removeFirst().color = Color.LIGHT_GRAY;
      } else {
        this.active = false;
        this.solved = true;
      }
    }
    
    if (this.solved) {
      if (!this.path.isEmpty()) {
        this.path.removeFirst().color = new Color(177, 156, 217);
      } else {
        this.active = false;
        this.solved = false;
      }
    }
  }
}

// to represent examples and tests
class ExamplesMaze {
  Maze world;
    
  Maze world1 = new Maze();
  Maze world2 = new Maze();
  Maze world3 = new Maze();
  Maze world4 = new Maze(10, 6);

  Vertex v1 = new Vertex(5,4);
  Vertex v2 = new Vertex(0, 0);
  Vertex v3 = new Vertex(10, 8);
  Vertex v4 = new Vertex(9, 5);
  Vertex v5 = new Vertex(3, 3);
  Vertex v6 = new Vertex(2, 2);
  Vertex v7 = new Vertex(2, 3);

  Edge e1 = new Edge(this.v1, this.v2, 5);
  Edge e2 = new Edge(this.v3, this.v4, 10);
  Edge e3 = new Edge(this.v4, this.v5, 16);
  Edge e4 = new Edge(this.v1, this.v2, 7);
  Edge e5 = new Edge(this.v2, this.v3, 9);

  ArrayList<Edge> edgeList1 = new ArrayList<Edge>();
  ArrayList<Edge> edgeList2 = new ArrayList<Edge>();
    
  ArrayList<Vertex> vertexList1 = new ArrayList<Vertex>();

  //start the game
  void testGame(Tester t) {
    world = new Maze(10, 6);
    world.bigBang(1000, 700, 0.1);
  }
    
  void init() {
    this.v1.outEdges = new ArrayList<>(Arrays.asList(e1, e2));
    this.v2.outEdges = new ArrayList<>(Arrays.asList(e2));
    this.v3.outEdges = new ArrayList<>(Arrays.asList(e1));
    this.v4.outEdges = new ArrayList<>(Arrays.asList(e3));
    this.v5.outEdges = new ArrayList<>(Arrays.asList(e1, e2, e3));
    this.v6.outEdges = new ArrayList<>(Arrays.asList(e4));
    this.v7.outEdges = new ArrayList<>();     
  }

  void init2() {
    HashMap<Vertex, Vertex> hash = new HashMap<Vertex, Vertex>();
    hash.put(this.v2, this.v2);
    hash.put(this.v3, this.v3);
    hash.put(this.v4, this.v4);
    this.world2.rep = hash;
  }
     
  //test method hasPathTo
  void testHasPathTo(Tester t) {
    this.init();

    t.checkExpect(this.v1.hasPathTo(this.v2), true);
    t.checkExpect(this.v3.hasPathTo(this.v5), true);
    t.checkExpect(this.v7.hasPathTo(this.v6), false);
    t.checkExpect(world4.vertices.get(0).get(0)
        .hasPathBetween(world4.vertices.get(5).get(9)), true);
  }
  
  //test method hasPathBetween
  void testHasPathBetween(Tester t) {
    this.init();

    t.checkExpect(this.v1.hasPathBetween(this.v2), true);
    t.checkExpect(this.v3.hasPathBetween(this.v5), true);
    t.checkExpect(this.v7.hasPathBetween(this.v6), false);
    t.checkExpect(world4.vertices.get(0).get(0)
        .hasPathBetween(world4.vertices.get(5).get(9)), true);
  }

  //test method equals
  void testEquals(Tester t) {
    t.checkExpect(this.v1.equals(this.v2), false);
    t.checkExpect(this.v2.equals(this.v2), true);
    t.checkExpect(this.e1.equals(this.e2), false);
    t.checkExpect(this.e3.equals(this.e3), true);
  }
  
  //test method hashCode
  void testHashCode(Tester t) {
    t.checkExpect(this.e1.hashCode(), 138);
    t.checkExpect(this.e2.hashCode(), 410);
  }
     
  //test method drawVertex
  void testDrawVertex(Tester t) {
    t.checkExpect(this.v1.drawVertex(), new RectangleImage(Maze.SCALE_WIDTH - 3,
        Maze.SCALE_HEIGHT - 3, "solid", new Color(255, 255, 255)));
    t.checkExpect(this.v2.drawVertex(),  new RectangleImage(Maze.SCALE_WIDTH - 3,
        Maze.SCALE_HEIGHT - 3, "solid", new Color(255, 255, 255)));
  }
     
  //test method drawVerticalEdge
  void testDrawVerticalEdge(Tester t) {
    t.checkExpect(this.e1.drawVerticalEdge(), new RectangleImage(3, Maze.SCALE_HEIGHT, 
        "solid", new Color(0, 0, 0)));
    t.checkExpect(this.e2.drawVerticalEdge(), new RectangleImage(3, Maze.SCALE_HEIGHT,
        "solid", new Color(0, 0, 0)));
  }

  //test method drawHorizontalEdge
  void testDrawHorizontalEdge(Tester t) {
    t.checkExpect(this.e2.drawHorizontalEdge(), new RectangleImage(Maze.SCALE_WIDTH, 3,
        "solid", new Color(0, 0, 0)));
    t.checkExpect(this.e3.drawHorizontalEdge(), new RectangleImage(Maze.SCALE_WIDTH, 3,
        "solid", new Color(0, 0, 0)));
  }

  //test method drawVerticalBranch
  void testDrawVerticalBranch(Tester t) {
    t.checkExpect(this.e2.drawVerticalBranch(), new RectangleImage(3, Maze.SCALE_HEIGHT,
        "solid", new Color(255, 255, 255)));
    t.checkExpect(this.e3.drawVerticalBranch(), new RectangleImage(3, Maze.SCALE_HEIGHT,
        "solid", new Color(255, 255, 255)));
  }

  // test method drawHorizontalBranch
  void testDrawHorizontalBranch(Tester t) {
    t.checkExpect(this.e2.drawHorizontalBranch(), new RectangleImage(Maze.SCALE_WIDTH, 3,
        "solid", new Color(255, 255, 255)));
    t.checkExpect(this.e3.drawHorizontalBranch(), new RectangleImage(Maze.SCALE_WIDTH, 3,
        "solid", new Color(255, 255, 255)));
  }
     
  //test initWorkList
  void testInitWorkList(Tester t) {
    ArrayList<Edge> arr = this.world1.workList;
    for (int i = 1; i < arr.size(); i++) {
      t.checkNumRange(arr.get(i).weight, arr.get(i - 1).weight - 1, arr.get(i).weight + 1);
    }  
  }

  //test method initRep
  void testInitRep(Tester t) {
    for (Vertex v : this.world1.vert) {
      t.checkExpect(this.world1.rep.containsKey(v), true);
    }
  }

  //test method build
  void testBuild(Tester t) {
    t.checkExpect(this.world1.edges.size(), this.world1.vert.size() - 1);
  }

  // to test method union
  void testUnion(Tester t) {
    this.init2();

    t.checkExpect(this.world2.rep.get(this.v2), this.v2);
    this.world2.union(this.v2, this.v3);
    t.checkExpect(this.world2.rep.get(this.v2), this.v3);
  }

  // to test method find
  void testFind(Tester t) {
    this.init2();

    t.checkExpect(this.world2.find(this.v2), this.v2);
    t.checkExpect(this.world2.find(this.v3), this.v3);
    t.checkExpect(this.world2.find(this.v4), this.v4);
  }

  //test method initMaze
  void testInitMaze(Tester t) {
    ArrayList<Edge> maze = this.world1.maze;
    ArrayList<Edge> edges = this.world1.edges;

    for (Edge e : maze) {
      for (Edge w : edges) {
        t.checkExpect(e.equals(w), false);
      }
    }
  }
}