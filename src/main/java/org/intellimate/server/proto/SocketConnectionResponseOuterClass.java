// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server-protobuf/socket_connection_response.proto

package org.intellimate.server.proto;

public final class SocketConnectionResponseOuterClass {
  private SocketConnectionResponseOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_intellimate_SocketConnectionResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_intellimate_SocketConnectionResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n0server-protobuf/socket_connection_resp" +
      "onse.proto\022\013intellimate\"&\n\030SocketConnect" +
      "ionResponse\022\n\n\002id\030\001 \001(\005B \n\034org.intellima" +
      "te.server.protoP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_intellimate_SocketConnectionResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_intellimate_SocketConnectionResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_intellimate_SocketConnectionResponse_descriptor,
        new java.lang.String[] { "Id", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
