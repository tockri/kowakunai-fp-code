package dev.tockri.kowakunai.args;

class ArgvIterator {
  private final String[] argv;
  private int index;

  ArgvIterator(String[] argv) {
    this.argv = argv;
    index = 0;
  }

  boolean hasNext() {
    return index < argv.length;
  }

  String next() {
    return hasNext() ? argv[index++] : null;
  }
}
