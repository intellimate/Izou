// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: server-protobuf/http_response.proto

package org.intellimate.server.proto;

public final class HttpResponseOuterClass {
  private HttpResponseOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_intellimate_HttpResponse_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_intellimate_HttpResponse_fieldAccessorTable;
  static com.google.protobuf.Descriptors.Descriptor
    internal_static_intellimate_HttpResponse_Header_descriptor;
  static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_intellimate_HttpResponse_Header_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n#server-protobuf/http_response.proto\022\013i" +
      "ntellimate\"\237\001\n\014HttpResponse\022\016\n\006status\030\001 " +
      "\001(\005\0221\n\007headers\030\002 \003(\0132 .intellimate.HttpR" +
      "esponse.Header\022\023\n\013contentType\030\003 \001(\t\022\021\n\tb" +
      "ody_size\030\004 \001(\022\032$\n\006Header\022\013\n\003key\030\001 \001(\t\022\r\n" +
      "\005value\030\002 \003(\tB \n\034org.intellimate.server.p" +
      "rotoP\001b\006proto3"
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
    internal_static_intellimate_HttpResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_intellimate_HttpResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_intellimate_HttpResponse_descriptor,
        new java.lang.String[] { "Status", "Headers", "ContentType", "BodySize", });
    internal_static_intellimate_HttpResponse_Header_descriptor =
      internal_static_intellimate_HttpResponse_descriptor.getNestedTypes().get(0);
    internal_static_intellimate_HttpResponse_Header_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_intellimate_HttpResponse_Header_descriptor,
        new java.lang.String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
