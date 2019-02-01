package instantiator.pairing.interfaces;

public interface TouchSource {

  void register(TouchReceiver receiver);
  void unregister(TouchReceiver receiver);

}
