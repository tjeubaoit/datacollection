namespace java thrift.collect
namespace go thrift.collect

service FbAvatarService {

   string fetchAvatarUrl(1:string id);
}

service IdGenerator {

    i64 generate(1:list<string> seeds, 2:i64 def_val);
}