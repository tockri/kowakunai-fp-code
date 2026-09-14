package dev.tockri.kowakunai.args.builder;

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
    return argv[index++];
  }
}
